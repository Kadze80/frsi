package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class Form implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {INPUT, INPUT_RAW, OUTPUT, CONSOLIDATED}

	public static Map<String, String> resMap = new HashMap<String, String>();
	static {
		resMap.put("kz_INPUT", "Кіріс");
		resMap.put("kz_INPUT_RAW", "Құрылымсыз");
		resMap.put("kz_OUTPUT", "Шығыс");
		resMap.put("kz_CONSOLIDATED", "Құрама");

		resMap.put("en_INPUT", "Input");
		resMap.put("en_INPUT_RAW", "Input raw");
		resMap.put("en_OUTPUT", "Output");
		resMap.put("en_CONSOLIDATED", "Consolidated");

		resMap.put("ru_INPUT", "Входной");
		resMap.put("ru_INPUT_RAW", "Входной неструктурированный");
		resMap.put("ru_OUTPUT", "Выходной");
		resMap.put("ru_CONSOLIDATED", "Сводный");
	}

	private Long id;
	private String code;
	private String typeCode;
	private String typeName;

	private FormHistory formHistory;

	public String getTypeName(String languageCode) {
		String result = resMap.get(languageCode + "_" + typeCode);
		return result == null ? "" : result;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public FormHistory getFormHistory() {
		return formHistory;
	}

	public void setFormHistory(FormHistory formHistory) {
		this.formHistory = formHistory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Form form = (Form) o;

		if (!id.equals(form.id)) return false;

		return true;
	}



	@Override
	public String toString() {
		return "Form{ id=" + id + ", code='" + code + '\'' + "}";
	}
}
