package util;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.*;
import java.util.Date;
import java.util.Map;

public class Convert {

    public static final String HEXES = "0123456789ABCDEF";

    public static final DateFormat dateFormatIso = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat dateFormatRus = new SimpleDateFormat("dd.MM.yyyy");
    public static final DateFormat dateFormatCompact = new SimpleDateFormat("yyyyMMdd");
    public static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat dateTimeFormatIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateFormat dateTimeFormatRus = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static final DateFormat dateTimeFormatCompact = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat dateTimeFormatCompact_ = new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static final DateFormat timeFormatMs = new SimpleDateFormat("HH:mm:ss.SSS");
    public static final DateFormat[] dateFormats = {dateFormatIso, dateFormatRus, dateFormatCompact};
    public static final DateFormat[] dateTimeFormats = {dateTimeFormat, dateTimeFormatIso, dateTimeFormatRus, dateTimeFormatCompact, dateTimeFormatCompact_};
    public static final DateFormat[] timeFormats = {timeFormat, timeFormatMs};

    public static String getHex(byte[] bytes, boolean reverse) {
        if (bytes == null) return null;
        final StringBuilder sb = new StringBuilder(2 * bytes.length);
        if (reverse) {
            for (int i = bytes.length - 1; i >= 0; i--)
                sb.append(HEXES.charAt((bytes[i] & 0xF0) >> 4)).append(HEXES.charAt((bytes[i] & 0x0F)));
        } else {
            for (int i = 0; i < bytes.length; i++)
                sb.append(HEXES.charAt((bytes[i] & 0xF0) >> 4)).append(HEXES.charAt((bytes[i] & 0x0F)));
        }
        return sb.toString();
    }

    public static byte[] readBytesFromBufferedInputStream(InputStream is) {
        BufferedInputStream bis = null;
        byte[] bytes = null;
        try {
            bis = new BufferedInputStream(is);
            bytes = new byte[bis.available()];
            bis.read(bytes);
            bis.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void writeBytesToBufferedOutputStream(byte[] bytes, OutputStream os) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write(bytes);
            bos.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getSerializedObject(Object object) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
            try {
                bos.close();
            } catch (IOException e) {
            }
        }
        return bytes;
    }

    public static Object getDeserializedObject(byte[] bytes) {
        Object object = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            object = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
            try {
                bis.close();
            } catch (IOException e) {
            }
        }
        return object;
    }

    public static String readStringFromBufferedInputStream(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) sb.append(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String htmlFormat(String srcString) {
        return srcString
                .replaceAll("&", "&amp;") // must be replaced first
                .replaceAll("\"", "&quot;")
                .replaceAll("\'", "&#39;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

    public static String htmlFormatWithTabsAndLineBreaks(String srcString) {
        return htmlFormat(srcString)
                .replaceAll("\\t", "&emsp;&emsp;")
                .replaceAll("\\n", "<br/>"); // line breaks must be replaced after replacing <>
    }

    public static String getCssRgbaFromHexRgba(String hexRgba) {
        StringBuilder sb = new StringBuilder();
        try {
            int r = Integer.parseInt(hexRgba.substring(2, 4), 16);
            int g = Integer.parseInt(hexRgba.substring(4, 6), 16);
            int b = Integer.parseInt(hexRgba.substring(6, 8), 16);
            float a = Integer.parseInt(hexRgba.substring(8, 10), 16) / 255f;
            sb.append("rgba(").append(r).append(",").append(g).append(",").append(b).append(",").append(a).append(")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getDateStringFromDate(Date date) {
        return dateFormatRus.format(date);
    }

    public static String getDateIsoStringFromDate(Date date) {
        return dateFormatIso.format(date);
    }

    public static String getDateTimeStringFromDate(Date date) {
        return dateTimeFormat.format(date);
    }

    public static String getDateTimeStringFromDateRus(Date date) {
        return dateTimeFormatRus.format(date);
    }

    public static String getDateTimeIsoStringFromDate(Date date) {
        return dateTimeFormatIso.format(date);
    }

    public static String getTimeStringFromDate(Date date) {
        return timeFormat.format(date);
    }

    public static String getTimeMsStringFromDate(Date date) {
        return timeFormatMs.format(date);
    }

    public static Date getDateFromString(String dateString) {
        for (DateFormat df : dateTimeFormats) {
            try {
                return df.parse(dateString);
            } catch (ParseException e) {
            }
        }
        for (DateFormat df : dateFormats) {
            try {
                return df.parse(dateString);
            } catch (ParseException e) {
            }
        }
        return null;
    }

    public static String getStringFromNumber(double value) {
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("");
        dfs.setGroupingSeparator(' ');
        dfs.setMonetaryDecimalSeparator('.');
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(value);
    }

    public static String getHashFromMap(Map<String, String> map, String algorithm) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(entry.getValue());
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            byte[] hash = messageDigest.digest(bytes);
            return (new HexBinaryAdapter()).marshal(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeURIComponent(String component) { // Compatible with JavaScript decodeURIComponent()
        String result = null;
        try {
            result = URLEncoder.encode(component, "UTF-8")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = component;
        }
        return result;
    }

    public static String getNumWithMaskFromStr(String value, String mask) throws Exception {
        try {
            switch (Util.getIntFromStr(mask == null ? "0" : mask)) {
                case 0:
                    value = new DecimalFormat("#").format(Long.valueOf(value));
                    break;
                case 1:
                    value = new DecimalFormat("#.#").format(Double.valueOf(value));
                    break;
                case 2:
                    value = new DecimalFormat("#.##").format(Double.valueOf(value));
                    break;
                case 3:
                    value = new DecimalFormat("#.###").format(Double.valueOf(value));
                    break;
                case 4:
                    value = new DecimalFormat("#.####").format(Double.valueOf(value));
                    break;
                case 5:
                    value = new DecimalFormat("#.#####").format(Double.valueOf(value));
                    break;
                case 6:
                    value = new DecimalFormat("#.######").format(Double.valueOf(value));
                    break;
                default:
                    value = new DecimalFormat("#").format(Double.valueOf(value));
                    break;
            }
            value = value.replace(",", ".");
        } catch (Exception e) {
            throw new Exception("Ошибка конвертирования значения " + value + " в числовой формат!");
        }
        return value;
    }

    public static String escapeSqlLikeSymbols(String text, char escapeChar) {
        String result = text;
        if (result != null) {
            result = result.replace("_", escapeChar + "_");
            result = result.replace("%", escapeChar + "%");
        }
        return result;
    }

    public static String getBrowserName(Map<String, String> requestHeaderMap) {
        String result = "unknown browser";
        String userAgent;
        if (requestHeaderMap != null) {
            userAgent = requestHeaderMap.get("User-Agent");
            if (userAgent != null) {
                if (userAgent.toLowerCase().indexOf("chrome") != -1) {
                    result = "Chrome";
                } else if (userAgent.toLowerCase().indexOf("firefox") != -1) {
                    result = "Mozilla";
                } else if (userAgent.toLowerCase().indexOf("opera") != -1) {
                    result = "Opera";
                } else if (userAgent.toLowerCase().indexOf("msie") != -1 || userAgent.indexOf("rv:") != -1) {
                    result = "IE";
                } else if (userAgent.toLowerCase().indexOf("safari") != -1) {
                    result = "Safari";
                }
            }
        }
        return result;
    }

    public static String getContentDespositionFilename(String fileName, Map<String, String> requestHeaderMap) {
        String result = fileName;
        String browserName = getBrowserName(requestHeaderMap);
        try {
            byte[] fileNameBytes = fileName.getBytes(browserName.equals("IE") ? "windows-1251" : "utf-8");
            String dispositionFileName = "";
            for (byte b : fileNameBytes) {
                dispositionFileName += (char) (b & 0xff);
            }
            result = dispositionFileName;
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        result = browserName.equals("Mozilla") ? "\"" + result + "\"" : result;
        return result;
    }

    public static double parseDouble(String initialValue) {
        double parsedValue;
        if (initialValue != null && !initialValue.trim().isEmpty())
            try {
                parsedValue = Double.parseDouble(initialValue.trim());
            } catch (NumberFormatException e) {
                throw new NumberFormatException(MessageFormat.format("Неверный формат для числа {0}", initialValue));
            }
        else
            parsedValue = 0;
        return parsedValue;
    }

    public static int parseInt(String initialValue) {
        int parsedValue;
        if (initialValue != null && !initialValue.trim().isEmpty())
            try {
                parsedValue = Integer.parseInt(initialValue.trim());
            } catch (NumberFormatException e) {
                throw new NumberFormatException(MessageFormat.format("Неверный формат для числа {0}", initialValue));
            }
        else
            parsedValue = 0;
        return parsedValue;
    }

    public static long parseLong(String initialValue) {
        long parsedValue;
        if (initialValue != null && !initialValue.trim().isEmpty())
            try {
                parsedValue = Long.parseLong(initialValue.trim());
            } catch (NumberFormatException e) {
                double d = parseDouble(initialValue);
                parsedValue = (long) d;
//                throw new NumberFormatException(MessageFormat.format("Неверный формат для числа {0}", initialValue));
            }
        else
            parsedValue = 0;
        return parsedValue;
    }

}