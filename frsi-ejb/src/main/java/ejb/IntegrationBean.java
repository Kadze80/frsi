package ejb;

import entities.NsiListItem;
import kz.gamma.asn1.ASN1InputStream;
import kz.gamma.asn1.DEROctetString;
import kz.gamma.asn1.DERSequence;
import kz.gamma.asn1.DERTaggedObject;
import kz.gamma.cms.Pkcs7Data;
import kz.gamma.jce.X509Principal;
import kz.gamma.jce.provider.GammaTechProvider;
import kz.gamma.util.encoders.Base64;
import org.apache.log4j.Logger;
import util.OcspRequest;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;

@Stateless
public class IntegrationBean implements IntegrationLocal, IntegrationRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static final Map<String, String> resMap = new HashMap<String, String>();

    //todo открыть при промышленном
    //public static final String[] CA_NAME_DETAILS = {"C=KZ","O=KISC","CN=KISC Root CA"};
    public static final String[] CA_NAME_DETAILS = {"C=KZ", "O=KISC", "CN=KISC Beta CA"};

    static {
        resMap.put("en_RESPONDENT_BIN_EMPTY", "Respondent's BIN is empty");
        resMap.put("en_SIGNATURE_IS_OVERDUE", "Signature is overdue");
        resMap.put("en_SIGNATURE_IS_NOT_ACTIVE_YET", "Signature is not active yet");
        resMap.put("en_WRONG_ROOT_CA_NAME", "Wrong root CA name");
        resMap.put("en_CA_VALIDATION_FAILED", "CA validation failed");
        resMap.put("en_SUBJECT_ALTERNATIVE_NAME_FIELD_IS_EMPTY", "Subject's alternative name field is empty");
        resMap.put("en_SIGNATURE_BIN_DOESNT_MATCH", "Signature BIN doesn't match");
        resMap.put("en_SIGNATURE_WITHOUT_BIN", "Signature without BIN");
        resMap.put("en_UNEXPECTED_EXCEPTION_DURING_SIGNATURE_VALIDATION", "Unexpected exception during signature validation");

        resMap.put("ru_RESPONDENT_BIN_EMPTY", "Пустой БИН респондента");
        resMap.put("ru_SIGNATURE_IS_OVERDUE", "Истёк срок действия подписи");
        resMap.put("ru_SIGNATURE_IS_NOT_ACTIVE_YET", "Подпись еще не активна");
        resMap.put("ru_WRONG_ROOT_CA_NAME", "Неверное имя УЦ");
        resMap.put("ru_CA_VALIDATION_FAILED", "Ошибка проверки УЦ");
        resMap.put("ru_SUBJECT_ALTERNATIVE_NAME_FIELD_IS_EMPTY", "Пустое поле дополнительного имени субъекта");
        resMap.put("ru_SIGNATURE_BIN_DOESNT_MATCH", "БИН в подписи не соответствует");
        resMap.put("ru_SIGNATURE_IDN_DOESNT_MATCH_KISC", "ИИН в ключе не соответствует ИИН в КЦМР");
        resMap.put("ru_SIGNATURE_IDN_DOESNT_MATCH_USER_IDN", "ИИН в ключе не соответствует ИИН пользователя ФРСП");
        resMap.put("ru_SIGNATURE_WITHOUT_BIN", "Подпись без БИН");
        resMap.put("ru_UNEXPECTED_EXCEPTION_DURING_SIGNATURE_VALIDATION", "Ошибка при проверке подписи");
    }

    private int pageReloadDuration = 60;
    private int autosaveDuration = 900;

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        String pageReloadDurationProp = System.getProperty("frsi.page_reload_duration");
        if (pageReloadDurationProp != null) {
            try {
                pageReloadDuration = Integer.parseInt(pageReloadDurationProp);
            } catch (NumberFormatException e) {
            }
        }
        String autosaveDurationProp = System.getProperty("frsi.autosave_duration");
        if (autosaveDurationProp != null) {
            try {
                autosaveDuration = Integer.parseInt(autosaveDurationProp);
            } catch (NumberFormatException e) {
            }
        }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    // Front-end Server

    @Override
    public Date getNewDateFromBackEndServer() {
        return new Date();
    }

    @Override
    public int getPageReloadDuration() {
        return pageReloadDuration;
    }

    @Override
    public int getAutosaveDuration() {
        return autosaveDuration;
    }

    // KISC

    @Override
    public String getSignatureInfo(String caUrl, String signature, String respondentBin, String respondentIdn, String languageCode) {
        languageCode = languageCode.toLowerCase();

        if (Security.getProvider(GammaTechProvider.PROVIDER_NAME) == null)
            Security.addProvider(new GammaTechProvider());
        if (respondentBin == null || respondentBin.trim().isEmpty())
            return "ERROR: " + resMap.get(languageCode + "_RESPONDENT_BIN_EMPTY");

        ASN1InputStream extensionStream = null;

        Pkcs7Data pkcs = new Pkcs7Data(Base64.decode(signature));
        X509Certificate certificate;
        try {
            certificate = pkcs.getCertificateOfSigner();

            Date now = new Date();
            if (now.after(certificate.getNotAfter())) {
                return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_IS_OVERDUE");
            }
            if (now.before(certificate.getNotBefore())) {
                return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_IS_NOT_ACTIVE_YET");
            }

            String distinguishedName = certificate.getSubjectX500Principal().getName();
            String issuerIdn = distinguishedName.substring(distinguishedName.indexOf("UID=IIN") + 7, distinguishedName.indexOf("UID=IIN") + 19);
            String issuerName = certificate.getIssuerX500Principal().getName();

            boolean correctCAName = true;
            List<String> caNameDetails = Arrays.asList(issuerName.split(","));
            if (caNameDetails.size() == CA_NAME_DETAILS.length) {
                for (String detail : CA_NAME_DETAILS) {
                    if (!caNameDetails.contains(detail)) {
                        correctCAName = false;
                        break;
                    }
                }
            } else
                correctCAName = false;

            if (!correctCAName) {
                return "ERROR: " + resMap.get(languageCode + "_WRONG_ROOT_CA_NAME");
            }

            try {
                OcspRequest.checkValid(certificate, caUrl);
            } catch (Exception e) {
                return "ERROR: " + resMap.get(languageCode + "_CA_VALIDATION_FAILED");
            }

            byte[] extensionBytes = certificate.getExtensionValue("2.5.29.17");
            if (extensionBytes == null) {
                return "ERROR: " + resMap.get(languageCode + "_SUBJECT_ALTERNATIVE_NAME_FIELD_IS_EMPTY");
            } else {
                extensionStream = new ASN1InputStream(extensionBytes);
                DEROctetString octetString = (DEROctetString) extensionStream.readObject();
                extensionStream.close();
                extensionStream = new ASN1InputStream(octetString.getOctets());
                DERSequence sequence = (DERSequence) extensionStream.readObject();
                extensionStream.close();
                Enumeration subjectAltNames = sequence.getObjects();
                String authority = null;
                String bin = null;
                String idn = null;
                while (subjectAltNames.hasMoreElements()) {
                    DERTaggedObject nextElement = (DERTaggedObject) subjectAltNames.nextElement();
                    X509Principal x509Principal = new X509Principal(nextElement.getObject().getEncoded());
                    String data = x509Principal.toString();
                    if (authority == null) {
                        authority = data;
                    } else {
                        int equalitySignIndex = data.lastIndexOf("=");
                        String value = data.substring(equalitySignIndex + 1);
                        if (bin == null) {
                            bin = value;
                            if (!bin.equals(respondentBin)) {
                                return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_BIN_DOESNT_MATCH");
                            }
                        }
                        if (issuerIdn != null && issuerIdn.equals(value)) {
                            idn = value;
                            if (!idn.equals(respondentIdn)) {
                                return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_IDN_DOESNT_MATCH_USER_IDN");
                            }
                        }
                    }
                }
                if (bin == null) return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_WITHOUT_BIN");
                if (idn == null) return "ERROR: " + resMap.get(languageCode + "_SIGNATURE_IDN_DOESNT_MATCH_KISC");
            }
            return distinguishedName;

        } catch (Exception e) {
            logger.debug(e);
            return "ERROR: " + resMap.get(languageCode + "_UNEXPECTED_EXCEPTION_DURING_SIGNATURE_VALIDATION");
        } finally {
            if (extensionStream != null) {
                try {
                    extensionStream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

}
