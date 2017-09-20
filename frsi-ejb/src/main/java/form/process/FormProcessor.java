package form.process;

import com.google.gson.Gson;
import entities.FormTag;
import form.tag.*;
import org.eclipse.persistence.exceptions.XMLMarshalException;
import util.Convert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.*;

public abstract class FormProcessor {

	public static final String ERR_UNCHECKED = "ERR_UNCHECKED";
	public static final String ERR_UNKNOWN = "ERR_UNKNOWN";
	public static final String ERR_OK = "ERR_OK";
	public static final String ERR_WRONG_XML = "ERR_WRONG_XML";

	public static final String ERR_NO_INPUT_NAME = "ERR_NO_INPUT_NAME";
	public static final String ERR_WRONG_INPUT_NAME = "ERR_WRONG_INPUT_NAME";

    protected static Map<String, String> resMap = new HashMap<String, String>();

    protected Form form;
	protected String xml;
	protected String formNameWithColon;
	protected boolean isValidationOk;
	protected String validationMessage;
	protected String errorMessage;
    protected String lang;
    protected LocationListener locationListener;

    private Gson gson = new Gson();

    static {
        resMap.put("en_wrongXml", "Wrong XML structure");
        resMap.put("en_Ok", "Ok");
        resMap.put("en_error", "Error");
        resMap.put("en_noFormName", "No form name");
        resMap.put("en_wrongFormName", "Wrong form name");
        resMap.put("en_noFormLanguage", "No form language");
        resMap.put("en_wrongFormLanguage", "Wrong form language");
        resMap.put("en_noFormBeginDate", "No form begin date");
        resMap.put("en_wrongFormBeginDate", "Wrong form begin date");
        resMap.put("en_noFormEndDate", "No form end date");
        resMap.put("en_wrongFormEndDate", "Wrong form end date");
        resMap.put("en_noFieldName", "No field name");
        resMap.put("en_wrongFieldName", "Wrong field name");
        resMap.put("en_noFormTypeCode", "No form type code");
        resMap.put("en_wrongFormTypeCode", "Wrong form type code");
        resMap.put("en_noXmlVersion", "No xml version");

        resMap.put("kz_wrongXml", "XML құрылымы дұрыс емес");
        resMap.put("kz_Ok", "Дұрыс");
        resMap.put("kz_error", "Қате");
        resMap.put("kz_noFormName", "Форманың аты жоқ");
        resMap.put("kz_wrongFormName", "Форманың аты дұрыс емес");
        resMap.put("kz_noFormLanguage", "Форманың тілі көрсетілмеген");
        resMap.put("kz_wrongFormLanguage", "Форманың тілі дұрыс көрсетілмеген");
        resMap.put("kz_noFormBeginDate", "Форманың басталу күні жоқ");
        resMap.put("kz_wrongFormBeginDate", "Форманың басталу күні дұрыс емес");
        resMap.put("kz_noFormEndDate", "Форманың аяқталу күні жоқ");
        resMap.put("kz_wrongFormEndDate", "Форманың аяқталу күні дұрыс емес");
        resMap.put("kz_noFieldName", "Еңгізу жолақтың аты жоқ");
        resMap.put("kz_wrongFieldName", "Еңгізу жолақтың аты дұрыс емес");
        resMap.put("kz_noFormTypeCode", "Форманың типінің коды жоқ");
        resMap.put("kz_wrongFormTypeCode", "Форманың типінің коды дұрыс емес");
        resMap.put("kz_noXmlVersion", "Xml файлдың нұсқасы жоқ");

        resMap.put("ru_wrongXml", "Неправильная структура XML");
        resMap.put("ru_Ok", "Правильно");
        resMap.put("ru_error", "Ошибка");
        resMap.put("ru_noFormName", "Нет имени у формы");
        resMap.put("ru_wrongFormName", "Неправильное имя у формы");
        resMap.put("ru_noFormLanguage", "Не указан язык для формы");
        resMap.put("ru_wrongFormLanguage", "Неверно указан язык для формы");
        resMap.put("ru_noFormBeginDate", "Нет даты начала действия формы");
        resMap.put("ru_wrongFormBeginDate", "Неправильная дата начала действия формы");
        resMap.put("ru_noFormEndDate", "Нет даты окончания действия формы");
        resMap.put("ru_wrongFormEndDate", "Неправильная дата окончания действия формы");
        resMap.put("ru_noFieldName", "Нет имени у поля ввода");
        resMap.put("ru_wrongFieldName", "Неправильное имя у поля ввода");
        resMap.put("ru_noFormTypeCode", "Нет кода типа формы");
        resMap.put("ru_wrongFormTypeCode", "Неправильный код типа формы");
        resMap.put("ru_noXmlVersion", "Нет версии Xml файла");
    }

    protected FormProcessor(String xml, String languageCode) {
        this.xml = xml;
        this.lang = languageCode.trim().toLowerCase();
        form = null;
        errorMessage = "";
        try {
            JAXBContext context = JAXBContext.newInstance(Form.class, Table.class, Tr.class, Th.class, Td.class,
                    Br.class, Span.class, P.class, H1.class, H2.class, H3.class, H4.class, H5.class, H6.class,
                    StaticText.class, InputText.class, InputSelect.class, InputDate.class, InputTime.class, Style.class, Script.class,
                    Script2.class, ServerScript.class, DynamicRow.class, Plus.class, Minus.class, PlusMinusBaDetails.class, Pick.class,
                    Calculate.class, Tbody.class, Thead.class, Div.class, ReceiverInput.class, DynamicCell.class, Button.class);

            //InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            //XMLInputFactory xif = XMLInputFactory.newFactory();
            //XMLStreamReader xsr = xif.createXMLStreamReader(is);

            //StringBuffer stringBuffer = new StringBuffer(xml);
            StringReader stringReader = new StringReader(xml);

            Unmarshaller unmarshaller = context.createUnmarshaller();
            //locationListener = new LocationListener(xsr);
            //unmarshaller.setListener(locationListener);
            form = (Form) unmarshaller.unmarshal(stringReader);
            if (form.tag != null && !form.tag.trim().isEmpty()) {
                form.formTag = gson.fromJson(form.tag, FormTag.class);
            }
//        } catch (XMLStreamException e) {
//            e.printStackTrace();
        } catch (JAXBException e) {
            Throwable le = e.getLinkedException();
            validationMessage = resMap.get(lang + "_wrongXml");
            if (le instanceof XMLMarshalException) {
                Throwable ie = ((XMLMarshalException)le). getInternalException();
                errorMessage = ie.toString().replace("<", "&lt;").replace(">", "&gt;");
				/*
				URL url1 = ie.getClass().getProtectionDomain().getCodeSource().getLocation();
				logger.debug("URL1=" + url1.toString());
				URL url2 = WstxParsingException.class.getProtectionDomain().getCodeSource().getLocation();
				logger.debug("URL2=" + url2.toString());

				logger.debug("1: " + ie.getClass().getClassLoader().toString());
				logger.debug("2: " + WstxParsingException.class.getClassLoader().toString());
				if (ie instanceof WstxParsingException) {
					WstxParsingException we = (WstxParsingException)ie;
					String msg = we.getMessage();
					int row = we.getLocation().getLineNumber();
					int col = we.getLocation().getColumnNumber();
					logger.debug(msg + ", line:" + row + ", col:" + col);
				}
				*/
            } else {
                errorMessage = le.getMessage();
            }
        }
        if (form == null) {
            formNameWithColon = ":";
        } else {
            formNameWithColon = form.name + ":";
            errorMessage = validate(form);
        }
        validationMessage = errorMessage.isEmpty() ? resMap.get(lang + "_Ok") : resMap.get(lang + "_error");
        isValidationOk = errorMessage.isEmpty();
    }

    public String getFormName() {
		return form.name;
	}
	public String getFormNameWithColon() {
		return formNameWithColon;
	}
	public String getFormLanguageCode() {
		return form.languageCode;
	}
	public String getFormTitle() {
		return form.title;
	}
    public String getFormShortName() {
        return form.shortName;
    }
	public Date getFormBeginDate() {
		return form.dateBeginDate;
	}
	public Date getFormEndDate() {
		return form.dateEndDate;
	}
	public String getFormTag() {
		return form.tag;
	}
	public String getFormXml() {
		return xml;
	}
    public Integer getFormXmlVersion(){
        return form.xmlVersion;
    }

    public String getFormTypeCode() {
        return form.typeCode;
    }

    public int getFormPeriodCount(){
        return form.periodCount;
    }

	public boolean isValidationOk() {
		return isValidationOk;
	}
	public String getValidationMessage() {
		return validationMessage;
	}
	public String getErrorMessage() {
		return errorMessage;
	}

    protected String validate(Container container) {
        StringBuilder sb = new StringBuilder();

        if (container instanceof Form) {
            Form form = (Form) container;
            if (form.name == null || form.name.isEmpty()) {
                sb.append(resMap.get(lang + "_noFormName")).append("<br/>");
            } else if (!form.name.matches("^[_$A-Za-z][\\w\\.\\-]*")) {
                sb.append(resMap.get(lang + "_wrongFormName")).append(" \"").append(form.name).append("\"<br/>");
            }
            if (form.languageCode == null || form.languageCode.isEmpty()) {
                sb.append(resMap.get(lang + "_noFormLanguage")).append("<br/>");
            } else if (!(form.languageCode.toLowerCase().equals("en") || form.languageCode.toLowerCase().equals("kz") || form.languageCode.toLowerCase().equals("ru"))) {
                sb.append(resMap.get(lang + "_wrongFormLanguage")).append(" \"").append(form.languageCode).append("\"<br/>");
            }
            if (form.beginDate == null || form.beginDate.isEmpty()) {
                sb.append(resMap.get(lang + "_noFormBeginDate")).append("<br/>");
            } else {
                form.dateBeginDate = Convert.getDateFromString(form.beginDate);
                if (form.dateBeginDate == null) {
                    sb.append(resMap.get(lang + "_wrongFormBeginDate")).append(" \"").append(form.beginDate).append("\"<br/>");
                }
            }
            if (form.endDate == null || form.endDate.isEmpty()) {
                //sb.append(resMap.get(lang + "_noFormEndDate")).append("<br/>");
            } else {
                form.dateEndDate = Convert.getDateFromString(form.endDate);
                if (form.dateEndDate == null) {
                    sb.append(resMap.get(lang + "_wrongFormEndDate")).append(" \"").append(form.endDate).append("\"<br/>");
                }
            }
            Set<String> formTypeCodes = new HashSet<String>();
            for(FormType formType: EnumSet.allOf(FormType.class)){
                formTypeCodes.add(formType.toString());
            }
            if (form.typeCode == null || form.typeCode.isEmpty()) {
                sb.append(resMap.get(lang + "_noFormTypeCode")).append("<br/>");
            } else if (!formTypeCodes.contains(form.typeCode)) {
                sb.append(resMap.get(lang + "_wrongFormTypeCode")).append(" \"").append(form.typeCode).append("\"<br/>");
            }

            if (form.xmlVersion == null || form.xmlVersion == 0) {
                sb.append(resMap.get(lang + "_noXmlVersion")).append("</br>");
            }
        }
        for (Object element : container.elements) {
            if (element instanceof Container) {
                sb.append(validate((Container) element));
            } else {
                if (element instanceof Input) {
                    Input input = (Input) element;
                    input.errorCode = ERR_OK;
                    String errorLocation = "[]";
                            /*
                            + locationListener.getLocation(element).getLineNumber() + ","
                            + locationListener.getLocation(element).getColumnNumber() + "]: ";
                            */
                    if (input.name == null) {
                        input.errorCode = ERR_NO_INPUT_NAME;
                        sb.append(errorLocation).append(resMap.get(lang + "_noFieldName")).append("<br/>");
                    } else if (!input.name.matches("^[_$A-Za-z][\\w\\.\\-@]*")) {
                        input.errorCode = ERR_WRONG_INPUT_NAME;
                        sb.append(errorLocation).append(resMap.get(lang + "_wrongFieldName")).append(" \"").append(input.name).append("\"<br/>");
                    }
                }
            }
        }
        return sb.toString();
    }

}
