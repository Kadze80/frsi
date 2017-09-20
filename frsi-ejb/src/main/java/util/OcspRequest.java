package util;

import kz.gamma.asn1.*;
import kz.gamma.asn1.ocsp.*;
import kz.gamma.functions.InstanceFunctions;
import kz.gamma.functions.OCSPFunctions;
import kz.gamma.functions.SignatureFunctions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OcspRequest {
    public static final Logger log = Logger.getLogger(OcspRequest.class.getSimpleName());

    private static final OCSPFunctions ocspFunctions = new OCSPFunctions();
    private static final InstanceFunctions instanceFunctions = new InstanceFunctions();
    private static final SignatureFunctions signatureFunctions = new SignatureFunctions();

    public static void checkValid(X509Certificate cer, String url) throws Exception {
        try {
            byte[] cert_der = cer.getEncoded();
            OCSPRequest request = ocspFunctions.createRequest(cert_der, InstanceFunctions.BYTES_DER, "web-portal_NBRK", null);
            byte[] data = request.getEncoded();
            byte[] response = sendRequest(url, data);
            int ocspResult = ocspFunctions.getOCSPStatus(response);
            String result = "";
            switch (ocspResult) {
                case OCSPResponseStatus.SUCCESSFUL:
                    result = "SUCCESSFUL";
                    break;
                case OCSPResponseStatus.MALFORMED_REQUEST:
                    result = "MALFORMED_REQUEST";
                    break;
                case OCSPResponseStatus.INTERNAL_ERROR:
                    result = "INTERNAL_ERROR";
                    break;
                case OCSPResponseStatus.TRY_LATER:
                    result = "TRY_LATER";
                    break;
                case OCSPResponseStatus.SIG_REQUIRED:
                    result = "SIG_REQUIRED";
                    break;
                case OCSPResponseStatus.UNAUTHORIZED:
                    result = "UNAUTHORIZED";
                    break;
                default:
                    result = "UNKNOWN RESULT";
                    break;
            }
            log.log(Level.INFO, "RESULT REQUEST OCSP: {0}", result);
            if (ocspResult != OCSPResponseStatus.SUCCESSFUL) {
                throw new RuntimeException("Ocsp request failed. " + result);
            }
            int res = getOCSPStatus(response);
            log.log(Level.INFO, "RESULT STATUS: {0}", res);
            if(res==1) {
                throw new RuntimeException("Certificate revoked");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @param serviceURL URL службы
     * @param request Запрос в ASN
     * @return Ответ службы
     *
     */
    private static byte[] sendRequest(String serviceURL, byte[] request) throws Exception {
        byte[] response = null;
        DataOutputStream printout = null;
        DataInputStream dataInputStream = null;
        try {
            URLConnection conn = new URL(serviceURL).openConnection();
            conn.setRequestProperty("content-type", "application/pkixcmp");
            conn.setDoOutput(true);
            printout = new DataOutputStream(conn.getOutputStream());
            printout.write(request);
            printout.flush();
            int responseSize = conn.getContentLength();
            dataInputStream = new DataInputStream(conn.getInputStream());
            response = new byte[responseSize];
            int totalRead = 0;
            while (totalRead < responseSize) {
                int bytesRead = dataInputStream.read(response, totalRead, responseSize - totalRead);
                if (bytesRead < 0) {
                    break;
                }
                totalRead += bytesRead;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (printout != null) {
                    printout.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
        return response;
    }

    /**
     * Получение статуса проверяемого сертификата из ответа OCSP. Также
     * осуществляется проверка подписи OCSP ответа
     *
     * @param response Подписанный ответ в DER кодировке (подписанная квитанция)
     * @return Статус OCSP ответа
     * @throws Exception
     */
    private static Integer getOCSPStatus(byte[] response) throws Exception {
        ASN1InputStream respStream = new ASN1InputStream(response);
        DERObject respObject = respStream.readObject();
        ASN1Sequence respSeq = (ASN1Sequence) respObject;
        OCSPResponse resp = new OCSPResponse(respSeq);
        ASN1OctetString octetString = resp.getResponseBytes().getResponse();
        ASN1InputStream basicOcspResponseStream = new ASN1InputStream(octetString.getOctets());
        DERObject derObject = basicOcspResponseStream.readObject();
        BasicOCSPResponse basicOCSPResponse = BasicOCSPResponse.getInstance(derObject);
        X509Certificate x509cert = instanceFunctions.getX509CertificateInstance(
                basicOCSPResponse.getCerts().getObjectAt(0).getDERObject().getDEREncoded());
        boolean result = signatureFunctions.verifySign(basicOCSPResponse.getTbsResponseData().getEncoded(),
                basicOCSPResponse.getSignature().getBytes(), x509cert.getPublicKey());
        if (!result) {
            throw new Exception("OCSP response signature is not valid");
        }
        DERSequence responses = (DERSequence) basicOCSPResponse.getTbsResponseData().getResponses();
        SingleResponse singleResponse = SingleResponse.getInstance(responses.getObjectAt(0));
        CertStatus certStatus = singleResponse.getCertStatus();
        return certStatus.getTagNo();
    }
}
