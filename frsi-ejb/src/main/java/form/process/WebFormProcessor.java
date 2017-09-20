package form.process;

import com.google.gson.Gson;
import entities.InputValueCheck;
import form.html.CssParser;
import form.tag.*;
import org.apache.log4j.Logger;
import util.Convert;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebFormProcessor extends FormProcessor {
	private static final Logger logger = Logger.getLogger("fileLogger");

	private String html;
	private String templateStyle;
	private String templateScript;
	private List<String> dynamicFunctions;
	private StringBuilder fbAddCells;
	private List<InputValueCheck> inputValueChecks;
	private String jsCode;
	private Map<String, List<String>> dynamicLabelTrIds = new HashMap<String, List<String>>();

	private Gson gson = new Gson();

    public WebFormProcessor(String xml, String languageCode, boolean forView) {
		super(xml, languageCode);
	    templateStyle = Util.getTextFromResource(getClass().getClassLoader(), "css/template.css");
	    templateScript = Util.getTextFromResource(getClass().getClassLoader(), "js/template.js");
		dynamicFunctions = new ArrayList<String>();
		inputValueChecks = new ArrayList<InputValueCheck>();

	    fbAddCells = new StringBuilder();
	    fbAddCells.append("function addCells(tableId, row, dynamicRowLabel) {");
	    fbAddCells.append("var rowId = row.id.substring(tableId.length + 1);");
	    fbAddCells.append("var drIdValue = '$DynamicRowId';");
	    fbAddCells.append("var spans = [];");
	    fbAddCells.append("var inputs = [];");
	    fbAddCells.append("var selects = [];");
		fbAddCells.append("var textareas = [];");

	    html = form == null ? "" : getHtml(form, forView);
    }

	private String getHtml(Container container, boolean forView) {
		return getHtml(container, null, forView);
	}

	private String getHtml(Container container, String dynamicRowLabel, boolean forView) {
        StringBuilder tagBuilder = new StringBuilder();
        for (Object element : container.elements) {
            String className = element.getClass().getSimpleName();

			// Create compound name for container using parent container's name
			if (element instanceof Container) {
				Container containerElement = (Container) element;
				containerElement.container = container; // set parent container
				containerElement.localName = containerElement.name;
				containerElement.name = (containerElement.name == null || containerElement.name.trim().isEmpty()) ?
					container.name : container.name + "_" + containerElement.name;
			}

			String style=null;
			String styleClass = null;
			if (element instanceof Component) {
				Component component = (Component) element;
				component.container = container; // set parent container
				style = component.style;
				styleClass = component.styleClass;
				if (style != null && style.trim().isEmpty()) style = null;
				if (styleClass != null && styleClass.trim().isEmpty()) styleClass = null;
			}

            //region String
			if (className.equals("String")) {
	            String s0 = (String) element;
	            String s1 = Convert.htmlFormat(s0);
				tagBuilder.append(s1);
			}
			//endregion
            //region Br
			else if (className.equals("Br")) {
				tagBuilder.append("<br/>");
			}
            //endregion
            //region Tbody
			else if (className.equals("Tbody")) {
				tagBuilder.append("<tbody").append(getStyleAttributes(style, styleClass)).append(">")
						.append(getHtml((Container) element, forView)).append("</tbody>");
			}
            //endregion
            //region Thead
			else if (className.equals("Thead")) {
				tagBuilder.append("<thead").append(getStyleAttributes(style, styleClass)).append(">")
						.append(getHtml((Container) element, forView)).append("</thead>");
			}
            //endregion
            //region Div
			else if (className.equals("Div")) {
				Div div = (Div) element;
				tagBuilder.append("<div");
				if (div.id != null && !div.id.trim().isEmpty()) tagBuilder.append(" id=\"" + div.id + "\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(getHtml((Container) element, forView)).append("</div>");
			}
            //endregion
            //region P
			else if (className.equals("P")) {
				tagBuilder.append("<p").append(getStyleAttributes(style, styleClass)).append(">")
					.append(getHtml((Container) element, forView)).append("</p>");
			}
            //endregion
            //region Span
			else if (className.equals("Span")) {
				tagBuilder.append("<span").append(getStyleAttributes(style, styleClass)).append(">")
					.append(getHtml((Container) element, forView)).append("</span>");
			}
            //endregion
            //region H1-H6
			else if (className.equals("H1") || className.equals("H2") || className.equals("H3") || className.equals("H4") || className.equals("H5") || className.equals("H6")) {
				String tag = className.toLowerCase();
				tagBuilder.append("<").append(tag).append(getStyleAttributes(style, styleClass)).append(">")
					.append(getHtml((Container) element, forView)).append("</").append(tag).append(">");
			}
            //endregion
            //region Button
			else if (className.equals("Button")) {
				Button button = (Button) element;
				tagBuilder.append("<input type=\"button\" ");
				if (button.id != null && !button.id.trim().isEmpty()) {
					tagBuilder.append("id=\"" + button.id + "\" ");
				}
				if (button.text != null) {
					tagBuilder.append("value=\"" + button.text + "\" ");
				}
				tagBuilder.append(getStyleAttributes(style, styleClass)).append("/>");
			}
            //endregion
            //region Table
			else if (className.equals("Table")) {
				Table table = (Table) element;
				if (table.id == null || table.id.trim().isEmpty()) table.id = table.name;

				if (table.valueCheckFunc != null && !table.valueCheckFunc.trim().isEmpty()) {
					InputValueCheck inputValueCheck = new InputValueCheck();
					inputValueCheck.setKey("");
					inputValueCheck.setInputType(className);
					inputValueCheck.setValueCheckFunc(table.valueCheckFunc);
					registerInputValueCheck(inputValueCheck, null);
				}

				tagBuilder.append("<table");
				if (table.id != null && !table.id.trim().isEmpty()) tagBuilder.append(" id=\"" + table.id + "\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(getHtml((Container) element, forView)).append("</table>");
			}
            //endregion
            //region Tr
			else if (className.equals("Tr")) {
				Tr tr = (Tr) element;
				tagBuilder.append("<tr");
				if (tr.id != null && !tr.id.trim().isEmpty()) {
					tagBuilder.append(" id=\"" + container.name + ":" + tr.id + "\"");
					if (tr.data_dynamicRowLabel != null) {
						if (!dynamicLabelTrIds.containsKey(tr.data_dynamicRowLabel))
							dynamicLabelTrIds.put(tr.data_dynamicRowLabel, new ArrayList<String>());
						dynamicLabelTrIds.get(tr.data_dynamicRowLabel).add(tr.id);
						tagBuilder.append(" data_dynamicRowLabel=\"").append(tr.data_dynamicRowLabel).append("\"");
					}
				}
				if (tr.fixed != null && !tr.fixed.trim().isEmpty()) tagBuilder.append(" fixed=\"").append(tr.fixed).append("\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(getHtml((Container) element, forView)).append("</tr>");
			}
            //endregion
            //region Th
			else if (className.equals("Th")) {
				Th th = (Th) element;
				tagBuilder.append("<th");
				if (th.colspan != null && !th.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(th.colspan).append("\"");
				if (th.rowspan != null && !th.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(th.rowspan).append("\"");
				if (th.fixleftcnt != null && !th.fixleftcnt.trim().isEmpty()) tagBuilder.append(" fixleftcnt=\"").append(th.fixleftcnt).append("\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">")
					.append(getHtml((Container) element, forView)).append("</th>");
			}
            //endregion
            //region Td
			else if (className.equals("Td")) {
				Td td = (Td) element;
				tagBuilder.append("<td");
				if (td.colspan != null && !td.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(td.colspan).append("\"");
				if (td.rowspan != null && !td.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(td.rowspan).append("\"");
				if (td.fixed != null && !td.fixed.trim().isEmpty()) tagBuilder.append(" fixed=\"").append(td.fixed).append("\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">")
					.append(getHtml((Container) element, forView)).append("</td>");
			}
            //endregion
            //region StaticText
			else if (className.equals("StaticText")) {
				StaticText staticText = (StaticText) element;
				tagBuilder.append("<span id=\"");
				if (container.name != null && !container.name.isEmpty()) tagBuilder.append(container.name);
				tagBuilder.append("*");
				if (staticText.name != null && !staticText.name.isEmpty()) tagBuilder.append(staticText.name);
				tagBuilder.append(":");
				if (staticText.key != null && !staticText.key.isEmpty()) tagBuilder.append(staticText.key);
				tagBuilder.append(":");
				if (staticText.keyValue != null && !staticText.keyValue.isEmpty()) tagBuilder.append(staticText.keyValue);
				tagBuilder.append("\"");
				tagBuilder.append(getStyleAttributes(style, styleClass));
				tagBuilder.append(">");
				if (staticText.value != null && !staticText.value.trim().isEmpty()) tagBuilder.append(staticText.value);
				tagBuilder.append("</span>");
			}
            //endregion
            //region InputText
			else if (className.equals("InputText")) {
				InputText inputText = (InputText) element;
				if (inputText.multiLine != null && inputText.multiLine) {
					tagBuilder.append("<textarea ");

					StringBuilder sbCompoundName = new StringBuilder();
					if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
					sbCompoundName.append("*");
					if (inputText.name != null && !inputText.name.isEmpty()) sbCompoundName.append(inputText.name);
					sbCompoundName.append(":");
					if (inputText.key != null && !inputText.key.isEmpty()) sbCompoundName.append(inputText.key);
					sbCompoundName.append(":");
					if (inputText.keyValue != null && !inputText.keyValue.isEmpty())
						sbCompoundName.append(inputText.keyValue);
					String compoundName = sbCompoundName.toString();

					tagBuilder.append(" id=\"" + compoundName + "\"");
					tagBuilder.append(" name=\"" + compoundName + "\"");

					if ((inputText.readonly != null && inputText.readonly.booleanValue()) || forView)
						tagBuilder.append(" readonly=\"readonly\"");

					tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
					tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
					tagBuilder.append(" onkeyup=\"textAreaAdjust.call(this, this);\"");

					if(!forView) {
						boolean updateCalculatedFieldsManually = form.formTag != null && form.formTag.updateCalculatedFieldsManually;
						if (!updateCalculatedFieldsManually && (inputText.auto == null || !inputText.auto.booleanValue()))
							tagBuilder.append(" onchange=\"updateCalculatedFields();textAreaAdjust.call(this, this);makeDirty();\"");
						else
							tagBuilder.append(" onchange=\"textAreaAdjust.call(this, this);makeDirty();\"");
					}

					if (inputText.mask != null && !inputText.mask.trim().isEmpty()) {
						if (inputText.mask.equals("money0"))
							styleClass = styleClass == null ? "maskMoney0" : styleClass + " maskMoney0";
						else if (inputText.mask.equals("money1"))
							styleClass = styleClass == null ? "maskMoney1" : styleClass + " maskMoney1";
						else if (inputText.mask.equals("money2"))
							styleClass = styleClass == null ? "maskMoney2" : styleClass + " maskMoney2";
						else if (inputText.mask.equals("money3"))
							styleClass = styleClass == null ? "maskMoney3" : styleClass + " maskMoney3";
						else if (inputText.mask.equals("money4"))
							styleClass = styleClass == null ? "maskMoney4" : styleClass + " maskMoney4";
						else if (inputText.mask.equals("money5"))
							styleClass = styleClass == null ? "maskMoney5" : styleClass + " maskMoney5";
						else if (inputText.mask.equals("money6"))
							styleClass = styleClass == null ? "maskMoney6" : styleClass + " maskMoney6";
						else if (inputText.mask.equals("money7"))
							styleClass = styleClass == null ? "maskMoney7" : styleClass + " maskMoney7";
						else if (inputText.mask.equals("money8"))
							styleClass = styleClass == null ? "maskMoney8" : styleClass + " maskMoney8";
					}
					if ((inputText.readonly != null && inputText.readonly.booleanValue()) || forView) {
						styleClass = styleClass == null ? "readOnly" : styleClass + " readOnly";
					}
					if (inputText.auto != null && inputText.auto.booleanValue()) {
						styleClass = styleClass == null ? "auto" : styleClass + " auto";
					}
					style = style == null ? "resize: vertical;" : style + " resize: vertical;";
					styleClass = styleClass == null ? "autoHeight" : styleClass + " autoHeight";
					tagBuilder.append(getStyleAttributes(style, styleClass));

					tagBuilder.append(">");
					if (inputText.value != null && !inputText.value.trim().isEmpty()) {
						tagBuilder.append(inputText.value);
					} else
						tagBuilder.append(" ");
					tagBuilder.append("</textarea>");

					//if (inputText.auto == null || !inputText.auto.booleanValue()) {
					InputValueCheck inputValueCheck = new InputValueCheck();
					inputValueCheck.setInputType(className);
					if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
					if (inputText.valueType != null && !inputText.valueType.isEmpty())
						inputValueCheck.setValueType(inputText.valueType);
					else if (inputText.mask != null && !inputText.mask.trim().isEmpty()) {
						if (inputText.mask.equals("money0"))
							inputValueCheck.setValueType("int");
						else
							inputValueCheck.setValueType("float");

						inputValueCheck.setMask(inputText.mask);

					}
					if (inputText.required == null || !inputText.required.booleanValue() || forView)
						inputValueCheck.setRequired(false);
					else
						inputValueCheck.setRequired(true);
					if (inputText.valueCheckFunc != null && !inputText.valueCheckFunc.isEmpty())
						inputValueCheck.setValueCheckFunc(inputText.valueCheckFunc);
					inputValueCheck.setAuto(inputText.auto != null && inputText.auto.booleanValue());
					if (inputText.ref != null && !inputText.ref.isEmpty()) inputValueCheck.setRef(inputText.ref);
					if (inputText.refCode != null && !inputText.refCode.isEmpty()) inputValueCheck.setRefCode(inputText.refCode);
					if (inputText.refCaption != null && !inputText.refCaption.isEmpty()) inputValueCheck.setRefCaption(inputText.refCaption);
					if(inputText.unique != null && inputText.unique.booleanValue()){
						inputValueCheck.setUnique(inputText.unique);
					}
					if(inputText.uniqueArea != null && !inputText.uniqueArea.isEmpty()){
						inputValueCheck.setUniqueArea(inputText.uniqueArea);
					}
					if(inputText.valueFunc != null && !inputText.valueFunc.isEmpty()){
						inputValueCheck.setValueFunc(inputText.valueFunc);
					}
					if (inputText.value != null && !inputText.value.trim().isEmpty()) {
						inputValueCheck.setDefaultValue(inputText.value);
					}
					if (inputText.readonly == null || !inputText.readonly.booleanValue())
						inputValueCheck.setReadonly(false);
					else
						inputValueCheck.setReadonly(true);
					if(inputText.valueFunc != null && !inputText.valueFunc.isEmpty()){
						inputValueCheck.setValueFunc(inputText.valueFunc);
					}
					registerInputValueCheck(inputValueCheck, dynamicRowLabel);
					//}
				} else {
					tagBuilder.append("<input type=\"text\"");

					StringBuilder sbCompoundName = new StringBuilder();
					if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
					sbCompoundName.append("*");
					if (inputText.name != null && !inputText.name.isEmpty()) sbCompoundName.append(inputText.name);
					sbCompoundName.append(":");
					if (inputText.key != null && !inputText.key.isEmpty()) sbCompoundName.append(inputText.key);
					sbCompoundName.append(":");
					if (inputText.keyValue != null && !inputText.keyValue.isEmpty())
						sbCompoundName.append(inputText.keyValue);
					String compoundName = sbCompoundName.toString();

					tagBuilder.append(" id=\"" + compoundName + "\"");
					tagBuilder.append(" name=\"" + compoundName + "\"");

					if ((inputText.readonly != null && inputText.readonly.booleanValue()) || forView)
						tagBuilder.append(" readonly=\"readonly\"");

					if(!forView) {
						boolean updateCalculatedFieldsManually = form.formTag != null && form.formTag.updateCalculatedFieldsManually;
						if (!updateCalculatedFieldsManually && (inputText.auto == null || !inputText.auto.booleanValue()))
							tagBuilder.append(" onchange=\"updateCalculatedFields();makeDirty();\"");
						else
							tagBuilder.append(" onchange=\"makeDirty();\"");
					}

					if (inputText.mask != null && !inputText.mask.trim().isEmpty()) {
						if (inputText.mask.equals("money0"))
							styleClass = styleClass == null ? "maskMoney0" : styleClass + " maskMoney0";
						else if (inputText.mask.equals("money1"))
							styleClass = styleClass == null ? "maskMoney1" : styleClass + " maskMoney1";
						else if (inputText.mask.equals("money2"))
							styleClass = styleClass == null ? "maskMoney2" : styleClass + " maskMoney2";
						else if (inputText.mask.equals("money3"))
							styleClass = styleClass == null ? "maskMoney3" : styleClass + " maskMoney3";
						else if (inputText.mask.equals("money4"))
							styleClass = styleClass == null ? "maskMoney4" : styleClass + " maskMoney4";
						else if (inputText.mask.equals("money5"))
							styleClass = styleClass == null ? "maskMoney5" : styleClass + " maskMoney5";
						else if (inputText.mask.equals("money6"))
							styleClass = styleClass == null ? "maskMoney6" : styleClass + " maskMoney6";
						else if (inputText.mask.equals("money7"))
							styleClass = styleClass == null ? "maskMoney7" : styleClass + " maskMoney7";
						else if (inputText.mask.equals("money8"))
							styleClass = styleClass == null ? "maskMoney8" : styleClass + " maskMoney8";
					}
					if ((inputText.readonly != null && inputText.readonly.booleanValue()) || forView) {
						styleClass = styleClass == null ? "readOnly" : styleClass + " readOnly";
					}
					if (inputText.auto != null && inputText.auto.booleanValue()) {
						styleClass = styleClass == null ? "auto" : styleClass + " auto";
					}
					tagBuilder.append(getStyleAttributes(style, styleClass));
					if (inputText.value != null && !inputText.value.trim().isEmpty()) {
						tagBuilder.append(" value=\"").append(inputText.value).append("\"");
					}

					tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
					tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
					tagBuilder.append("/>");

					//if (inputText.auto == null || !inputText.auto.booleanValue()) {
					InputValueCheck inputValueCheck = new InputValueCheck();
					inputValueCheck.setInputType(className);
					if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
					if (inputText.valueType != null && !inputText.valueType.isEmpty())
						inputValueCheck.setValueType(inputText.valueType);
					else if (inputText.mask != null && !inputText.mask.trim().isEmpty()) {
						if (inputText.mask.equals("money0"))
							inputValueCheck.setValueType("int");
						else
							inputValueCheck.setValueType("float");

						inputValueCheck.setMask(inputText.mask);
					}
					if (inputText.required == null || !inputText.required.booleanValue() || forView)
						inputValueCheck.setRequired(false);
					else
						inputValueCheck.setRequired(true);
					if (inputText.valueCheckFunc != null && !inputText.valueCheckFunc.isEmpty())
						inputValueCheck.setValueCheckFunc(inputText.valueCheckFunc);
					inputValueCheck.setAuto(inputText.auto != null && inputText.auto.booleanValue());
					if (inputText.ref != null && !inputText.ref.isEmpty()) inputValueCheck.setRef(inputText.ref);
                    if (inputText.refCode != null && !inputText.refCode.isEmpty()) inputValueCheck.setRefCode(inputText.refCode);
					if (inputText.refCaption != null && !inputText.refCaption.isEmpty()) inputValueCheck.setRefCaption(inputText.refCaption);
					if(inputText.unique != null && inputText.unique.booleanValue()){
						inputValueCheck.setUnique(inputText.unique);
					}
					if(inputText.uniqueArea != null && !inputText.uniqueArea.isEmpty()){
						inputValueCheck.setUniqueArea(inputText.uniqueArea);
					}
					if(inputText.valueFunc != null && !inputText.valueFunc.isEmpty()){
						inputValueCheck.setValueFunc(inputText.valueFunc);
					}
					if (inputText.value != null && !inputText.value.trim().isEmpty()) {
						inputValueCheck.setDefaultValue(inputText.value);
					}
					if (inputText.readonly == null || !inputText.readonly.booleanValue())
						inputValueCheck.setReadonly(false);
					else
						inputValueCheck.setReadonly(true);
						registerInputValueCheck(inputValueCheck, dynamicRowLabel);
					//}
				}
			}
            //endregion
            //region ReceiverInput
			else if (className.equals("ReceiverInput")) {
				ReceiverInput receiverInput = (ReceiverInput) element;

				StringBuilder sbCompoundName = new StringBuilder();
				if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
				sbCompoundName.append("*");
				if (receiverInput.name != null && !receiverInput.name.isEmpty()) sbCompoundName.append(receiverInput.name);
				sbCompoundName.append(":");
				if (receiverInput.key != null && !receiverInput.key.isEmpty()) sbCompoundName.append(receiverInput.key);
				sbCompoundName.append(":");
				if (receiverInput.keyValue != null && !receiverInput.keyValue.isEmpty())
					sbCompoundName.append(receiverInput.keyValue);
				String compoundName = sbCompoundName.toString();

				InputValueCheck inputValueCheck = new InputValueCheck();
				inputValueCheck.setInputType(className);
				if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
				if (receiverInput.valueType != null && !receiverInput.valueType.isEmpty())
					inputValueCheck.setValueType(receiverInput.valueType);

				if (receiverInput.required == null || !receiverInput.required.booleanValue() || forView)
					inputValueCheck.setRequired(false);
				else
					inputValueCheck.setRequired(true);
				if (receiverInput.valueCheckFunc != null && !receiverInput.valueCheckFunc.isEmpty())
					inputValueCheck.setValueCheckFunc(receiverInput.valueCheckFunc);
				inputValueCheck.setAuto(receiverInput.auto != null && receiverInput.auto.booleanValue());
				if (receiverInput.ref != null && !receiverInput.ref.isEmpty()) inputValueCheck.setRef(receiverInput.ref);
				if (receiverInput.refCode != null && !receiverInput.refCode.isEmpty()) inputValueCheck.setRefCode(receiverInput.refCode);
				if (receiverInput.refCaption != null && !receiverInput.refCaption.isEmpty()) inputValueCheck.setRefCaption(receiverInput.refCaption);
				if(receiverInput.unique != null && receiverInput.unique.booleanValue()){
					inputValueCheck.setUnique(receiverInput.unique);
				}
				if(receiverInput.uniqueArea != null && !receiverInput.uniqueArea.isEmpty()){
					inputValueCheck.setUniqueArea(receiverInput.uniqueArea);
				}
				if (receiverInput.readonly == null || !receiverInput.readonly.booleanValue())
					inputValueCheck.setReadonly(false);
				else
					inputValueCheck.setReadonly(true);
				if (receiverInput.value != null && !receiverInput.value.trim().isEmpty()) {
					inputValueCheck.setDefaultValue(receiverInput.value);
				}
				if(receiverInput.valueFunc != null && !receiverInput.valueFunc.isEmpty()){
					inputValueCheck.setValueFunc(receiverInput.valueFunc);
				}
				registerInputValueCheck(inputValueCheck, dynamicRowLabel);

				buildReceiverInputHtml(tagBuilder, compoundName,
						receiverInput.ref, receiverInput.refCode, receiverInput.refCaption,
						receiverInput.multiLine, receiverInput.multiValue, receiverInput.readonly, receiverInput.auto,
						styleClass, style, forView);
			}
            //endregion
            //region InputDate
			else if (className.equals("InputDate")) {
				InputDate inputDate = (InputDate) element;
				tagBuilder.append("<input type=\"text\"");

				StringBuilder sbCompoundName = new StringBuilder();
				if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
				sbCompoundName.append("*");
				if (inputDate.name != null && !inputDate.name.isEmpty()) sbCompoundName.append(inputDate.name);
				sbCompoundName.append(":");
				if (inputDate.key != null && !inputDate.key.isEmpty()) sbCompoundName.append(inputDate.key);
				sbCompoundName.append(":");
				if (inputDate.keyValue != null && !inputDate.keyValue.isEmpty()) sbCompoundName.append(inputDate.keyValue);
				String compoundName = sbCompoundName.toString();

				tagBuilder.append(" id=\"" + compoundName + "\"");
				tagBuilder.append(" name=\"" + compoundName + "\"");

				if ((inputDate.readonly != null && inputDate.readonly.booleanValue()) || forView){
					tagBuilder.append(" readonly=\"readonly\"");
					styleClass = "readOnly";
				}else{
					styleClass = styleClass == null ? "datepicker" : styleClass + " datepicker";
				}

				tagBuilder.append(getStyleAttributes(style, styleClass));

				if (inputDate.value != null && !inputDate.value.trim().isEmpty()) {
					tagBuilder.append(" value=\"").append(inputDate.value).append("\"");
				}

				tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
				if(!forView){
					tagBuilder.append(" onchange=\"makeDirty();\"");
				}
				//tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
				tagBuilder.append("/>");

//				if ((inputDate.readonly == null || !inputDate.readonly.booleanValue()) && !forView) {
				InputValueCheck inputValueCheck = new InputValueCheck();
				inputValueCheck.setInputType(className);
				if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
				if (inputDate.valueType != null && !inputDate.valueType.isEmpty())
					inputValueCheck.setValueType(inputDate.valueType);
				if (inputDate.required == null || !inputDate.required.booleanValue() || forView)
					inputValueCheck.setRequired(false);
				else
					inputValueCheck.setRequired(true);
				if (inputDate.valueCheckFunc != null && !inputDate.valueCheckFunc.isEmpty())
					inputValueCheck.setValueCheckFunc(inputDate.valueCheckFunc);
				if (inputDate.unique != null && inputDate.unique.booleanValue()) {
					inputValueCheck.setUnique(inputDate.unique);
				}
				if (inputDate.uniqueArea != null && !inputDate.uniqueArea.isEmpty()) {
					inputValueCheck.setUniqueArea(inputDate.uniqueArea);
				}
				if (inputDate.readonly == null || !inputDate.readonly.booleanValue())
					inputValueCheck.setReadonly(false);
				else
					inputValueCheck.setReadonly(true);
				if (inputDate.value != null && !inputDate.value.isEmpty()) {
					inputValueCheck.setDefaultValue(inputDate.value);
				}
				if(inputDate.valueFunc != null && !inputDate.valueFunc.isEmpty()){
					inputValueCheck.setValueFunc(inputDate.valueFunc);
				}

					registerInputValueCheck(inputValueCheck, dynamicRowLabel);
//				}
			}
            //endregion
            //region InputTime
			else if (className.equals("InputTime")) {
				InputTime inputTime = (InputTime) element;
				tagBuilder.append("<input type=\"text\"");

				StringBuilder sbCompoundName = new StringBuilder();
				if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
				sbCompoundName.append("*");
				if (inputTime.name != null && !inputTime.name.isEmpty()) sbCompoundName.append(inputTime.name);
				sbCompoundName.append(":");
				if (inputTime.key != null && !inputTime.key.isEmpty()) sbCompoundName.append(inputTime.key);
				sbCompoundName.append(":");
				if (inputTime.keyValue != null && !inputTime.keyValue.isEmpty()) sbCompoundName.append(inputTime.keyValue);
				String compoundName = sbCompoundName.toString();

				tagBuilder.append(" id=\"" + compoundName + "\"");
				tagBuilder.append(" name=\"" + compoundName + "\"");

				if ((inputTime.readonly != null && inputTime.readonly.booleanValue()) || forView) tagBuilder.append(" readonly=\"readonly\"");
				styleClass = styleClass == null ? "timepicker" : styleClass + " timepicker";

				tagBuilder.append(getStyleAttributes(style, styleClass));
				if (inputTime.value != null && !inputTime.value.trim().isEmpty()) {
					tagBuilder.append(" value=\"").append(inputTime.value).append("\"");
				}

				tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
				//tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
				tagBuilder.append("/>");

//				if ((inputTime.readonly == null || !inputTime.readonly.booleanValue()) && !forView) {
				InputValueCheck inputValueCheck = new InputValueCheck();
				inputValueCheck.setInputType(className);
				if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
				if (inputTime.valueType != null && !inputTime.valueType.isEmpty())
					inputValueCheck.setValueType(inputTime.valueType);
				if (inputTime.required == null || !inputTime.required.booleanValue() || forView)
					inputValueCheck.setRequired(false);
				else
					inputValueCheck.setRequired(true);
				if (inputTime.valueCheckFunc != null && !inputTime.valueCheckFunc.isEmpty())
					inputValueCheck.setValueCheckFunc(inputTime.valueCheckFunc);
				if (inputTime.unique != null && inputTime.unique.booleanValue()) {
					inputValueCheck.setUnique(inputTime.unique);
				}
				if (inputTime.uniqueArea != null && !inputTime.uniqueArea.isEmpty()) {
					inputValueCheck.setUniqueArea(inputTime.uniqueArea);
				}
				if (inputTime.readonly == null || !inputTime.readonly.booleanValue())
					inputValueCheck.setReadonly(false);
				else
					inputValueCheck.setReadonly(true);
				if (inputTime.value != null && !inputTime.value.isEmpty()) {
					inputValueCheck.setDefaultValue(inputTime.value);
				}
				if(inputTime.valueFunc != null && !inputTime.valueFunc.isEmpty()){
					inputValueCheck.setValueFunc(inputTime.valueFunc);
				}
				registerInputValueCheck(inputValueCheck, dynamicRowLabel);
//				}
			}
            //endregion
            //region InputSelect
			else if (className.equals("InputSelect")) {
				InputSelect inputSelect = (InputSelect) element;
				int widthEm = -1;
				if (style != null) {
					CssParser cssParser = new CssParser(style);
					String width = cssParser.getPropertyValue("width");
					if (width != null) {
						if (width.indexOf("em") > 0) {
							try {
								widthEm = Integer.parseInt(width.substring(0, width.indexOf("em")));

								StringBuilder styleBuilder = new StringBuilder();
								for (String prop : cssParser.getProperties()) {
									if (!prop.toLowerCase().equals("width"))
										styleBuilder.append(prop).append(":").append(cssParser.getPropertyValue(prop)).append(";");
								}
								styleBuilder.append("width:").append(widthEm - 3).append("em;");
								style = styleBuilder.toString();
							} catch (NumberFormatException e) {
							}
						}
					}
				}

				StringBuilder sbCompoundName = new StringBuilder();
				if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
				sbCompoundName.append("*");
				if (inputSelect.name != null && !inputSelect.name.isEmpty()) sbCompoundName.append(inputSelect.name);
				sbCompoundName.append(":");
				if (inputSelect.key != null && !inputSelect.key.isEmpty()) sbCompoundName.append(inputSelect.key);
				sbCompoundName.append(":");
				if (inputSelect.keyValue != null && !inputSelect.keyValue.isEmpty()) sbCompoundName.append(inputSelect.keyValue);
				String compoundName = sbCompoundName.toString();

				if (forView) {
					buildReceiverInputHtml(tagBuilder, compoundName,
							inputSelect.ref, inputSelect.refCode, inputSelect.refCaption,
							true, inputSelect.multiValue, inputSelect.readonly, inputSelect.auto,
							styleClass, style, forView);
				} else {

					tagBuilder.append("<div style=\"position: relative;");
					if (widthEm > -1) {
						tagBuilder.append(" width:" + widthEm + "em; text-align:left; ");
					}
					tagBuilder.append("\" class=\"ref-value\">");
					tagBuilder.append("<textarea readonly=\"readonly\" rows=\"2\"");

					String htmlTextareaIdPrefix = "textarea:";
					tagBuilder.append(" id=\"" + htmlTextareaIdPrefix + compoundName + "\"");
					style = style == null ? "resize: vertical;" : style + " resize: vertical;";
					styleClass = styleClass == null ? "inputSelect autoHeight" : styleClass + " inputSelect autoHeight";
					if (inputSelect.readonly != null && inputSelect.readonly.booleanValue()) {
						styleClass += " readOnly";
					}
					tagBuilder.append(getStyleAttributes(style, styleClass));
					tagBuilder.append(" ref=\"" + inputSelect.ref + "\"");
					tagBuilder.append(" refCode=\"" + inputSelect.refCode + "\"");
					tagBuilder.append(" refCaption=\"" + inputSelect.refCaption + "\"");
					if (inputSelect.multiValue != null && inputSelect.multiValue.booleanValue())
						tagBuilder.append(" multiValue=\"multiValue\"");
					if (inputSelect.viewModel != null && !inputSelect.viewModel.isEmpty()) {
						tagBuilder.append(" viewModel=\"" + inputSelect.viewModel + "\"");
					}
					tagBuilder.append("> </textarea>");

					tagBuilder.append("<input type=\"hidden\"");
					tagBuilder.append(" id=\"" + compoundName + "\"");
					tagBuilder.append(" name=\"" + compoundName + "\"");
					if (!forView) {
						tagBuilder.append(" onchange=\"makeDirty();\"");
					}
					tagBuilder.append("/><button type=\"button\" class=\"toolButton\" onclick=\"overlay.call(this);\" style=\"top: 0; position: absolute\"");
					if ((inputSelect.readonly != null && inputSelect.readonly.booleanValue()) || forView)
						tagBuilder.append(" disabled=\"disabled\"");
					tagBuilder.append(">...</button>");
					tagBuilder.append("</div>");
				}


				InputValueCheck inputValueCheck = new InputValueCheck();
				inputValueCheck.setInputType(className);
				if (compoundName != null && !compoundName.isEmpty()) inputValueCheck.setKey(compoundName);
				if (inputSelect.valueType != null && !inputSelect.valueType.isEmpty())
					inputValueCheck.setValueType(inputSelect.valueType);
				if (inputSelect.required == null || !inputSelect.required.booleanValue() || forView)
					inputValueCheck.setRequired(false);
				else
					inputValueCheck.setRequired(true);
				if (inputSelect.valueCheckFunc != null && !inputSelect.valueCheckFunc.isEmpty())
					inputValueCheck.setValueCheckFunc(inputSelect.valueCheckFunc);
				if (inputSelect.ref != null && !inputSelect.ref.isEmpty()) inputValueCheck.setRef(inputSelect.ref);
				if (inputSelect.refCode != null && !inputSelect.refCode.isEmpty())
					inputValueCheck.setRefCode(inputSelect.refCode);
				if (inputSelect.refCaption != null && !inputSelect.refCaption.isEmpty())
					inputValueCheck.setRefCaption(inputSelect.refCaption);
				if (inputSelect.multiValue != null && inputSelect.multiValue)
					inputValueCheck.setMultiValue(true);
				if (inputSelect.unique != null && inputSelect.unique.booleanValue()) {
					inputValueCheck.setUnique(inputSelect.unique);
				}
				if (inputSelect.uniqueArea != null && !inputSelect.uniqueArea.isEmpty()) {
					inputValueCheck.setUniqueArea(inputSelect.uniqueArea);
				}
				if (inputSelect.readonly == null || !inputSelect.readonly.booleanValue())
					inputValueCheck.setReadonly(false);
				else
					inputValueCheck.setReadonly(true);
				if(inputSelect.value!=null && !inputSelect.value.isEmpty()){
					inputValueCheck.setDefaultValue(inputSelect.value);
				}
				if(inputSelect.valueFunc != null && !inputSelect.valueFunc.isEmpty()){
					inputValueCheck.setValueFunc(inputSelect.valueFunc);
				}
				registerInputValueCheck(inputValueCheck, dynamicRowLabel);
			}
            //endregion
            //region DynamicRow
			else if (className.equals("DynamicRow")) {
				DynamicRow dynamicRow = (DynamicRow) element;
	            Container parent;
	            // Retrieve closest table
	            Table table = null;
	            parent = dynamicRow.container;
	            while (parent != null) {
		            if (parent instanceof Table) {
			            table = (Table) parent;
			            break;
		            } else {
			            parent = parent.container;
		            }
	            }
	            String tableId = table == null ? "" : table.id;

				// Generate HTML for a template row
				StringBuilder sbRowHtml = new StringBuilder();
	            // Generate inner HTML for each cell
				List<String> cellsInnerHtml = new ArrayList<String>();
				for (Object drElement : dynamicRow.elements) {
					if (drElement instanceof Td || drElement instanceof DynamicCell) {
						// Create compound name for td using parent container's name
						Container td = (Container) drElement;
						td.container = dynamicRow; // set parent container
						td.name = (td.name == null || td.name.trim().isEmpty()) ? dynamicRow.name : dynamicRow.name + "_" + td.name;
						// Get inner Html of each cell
						String innerHtml = getHtml(td, dynamicRow.label, forView).replaceAll("\\s+", " ").trim();
						cellsInnerHtml.add(innerHtml);
						if (td instanceof Td) {
							sbRowHtml.append("<td>").append(innerHtml).append("</td>");
						} else if (td instanceof DynamicCell) {
							sbRowHtml.append("<td id=\"@DynamicCell\">").append(innerHtml).append("</td>");
						}
					}
				}

//	            for (String cellHtml : cellsInnerHtml) sbRowHtml.append("<td>").append(cellHtml).append("</td>");
			tagBuilder.append("<tr id=\"" + dynamicRow.name + ":" + dynamicRow.groupId + ".$DynamicRowIdMinor" + "\"");
			if (dynamicRow.label != null)
				tagBuilder.append(" data_dynamicRowLabel=\"" + dynamicRow.label + "\"");
			tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(sbRowHtml.toString()).append("</tr>");

				// Generate dynamic function
				StringBuilder fb = new StringBuilder();
				if (dynamicRow.label != null)
					fb.append("if (tableId === '" + tableId + "' && dynamicRowLabel === '" + dynamicRow.label + "') {");
				else
					fb.append("if (tableId === '" + tableId + "' && dynamicRowLabel === '') {");
				for (int i = 0; i < cellsInnerHtml.size(); i++) {
					fb.append("var cell" + i + " = row.insertCell(" + i + ");");
					fb.append("cell" + i + ".innerHTML = '" + cellsInnerHtml.get(i) + "';");
					// processing spans
					fb.append("spans = cell" + i + ".getElementsByTagName('span');");
					fb.append("for (var i = 0; i < spans.length; i++) {");
					fb.append("  if (spans[i].innerHTML.indexOf(drIdValue) > -1) spans[i].innerHTML = rowId.replace('$D.','').replace('group.','');");
					fb.append("}");
					// processing inputs
					fb.append("inputs = cell" + i + ".getElementsByTagName('input');");
					fb.append("for (var i = 0; i < inputs.length; i++) {");
					fb.append("  var inputType = inputs[i].getAttribute('type');");
					fb.append("  if (inputs[i].id) inputs[i].id = inputs[i].id.replace(drIdValue, rowId);");
					fb.append("  if (inputs[i].name) inputs[i].name = inputs[i].name.replace(drIdValue, rowId);");
					fb.append("  if ($(inputs[i]).is('[class*=maskMoney]')) initMask(inputs[i]);");
					fb.append("  if ($(inputs[i]).hasClass('datepicker')) initDatePicker(inputs[i]);");
					fb.append("  if ($(inputs[i]).hasClass('timepicker')) initTimePicker(inputs[i]);");
					fb.append("  if (inputType === 'button' && inputs[i].onclick) {");
					fb.append("    var func = inputs[i].getAttribute('onclick');");
					fb.append("    func = func.replace(drIdValue, rowId);");
					fb.append("    inputs[i].setAttribute('onclick', func);");
					fb.append("  }");
					fb.append("}");
					// processing selects
					fb.append("selects = cell" + i + ".getElementsByTagName('select');");
					fb.append("for (var i = 0; i < selects.length; i++) {");
					fb.append("  if (selects[i].id) selects[i].id = selects[i].id.replace(drIdValue, rowId);");
					fb.append("  if (selects[i].name) selects[i].name = selects[i].name.replace(drIdValue, rowId);");
					fb.append("}");
					// processing textareas
					fb.append("textareas = cell" + i + ".getElementsByTagName('textarea');");
					fb.append("for (var i = 0; i < textareas.length; i++) {");
					fb.append("  if (textareas[i].id) textareas[i].id = textareas[i].id.replace(drIdValue, rowId);");
					fb.append("  if (textareas[i].name) textareas[i].name = textareas[i].name.replace(drIdValue, rowId);");
					fb.append("}");
				}
				fb.append("}");
				fbAddCells.append(fb.toString());
			}
            //endregion
            //region DynamicCell
			else if(className.equals("DynamicCell")) {
				DynamicCell dynamicCell = (DynamicCell) element;
				String tag;
				if (dynamicCell.header) {
					tag = "th";
				} else {
					tag = "td";
				}
				tagBuilder.append("<" + tag + " id=\"@DynamicCell\"");
				tagBuilder.append(getStyleAttributes(style, styleClass)).append(">")
						.append(getHtml((Container) element, forView)).append("</" + tag + ">");
			}
            //endregion
            //region Plus
			else if (className.equals("Plus")) {
				if(!forView){
					Plus plus = (Plus) element;
					Container parent;
					// Retrieve closest table
					Table table = null;
					parent = plus.container;
					while (parent != null) {
						if (parent instanceof Table) {
							table = (Table) parent;
							break;
						} else {
							parent = parent.container;
						}
					}
					if (table != null) plus.tableId = table.id;
					// Retrieve closest row
					Tr tr = null;
					parent = plus.container;
					while (parent != null) {
						if (parent instanceof Tr) {
							tr = (Tr) parent;
							break;
						} else {
							parent = parent.container;
						}
					}
					if (tr != null && tr.id != null) {
						String groupId = tr.id.substring(tr.id.lastIndexOf(":") + 1);
						plus.rowId = groupId;
					}
					String data_dynamicRowLabel = tr.data_dynamicRowLabel == null ? "" : tr.data_dynamicRowLabel;
					tagBuilder.append("<input type=\"button\" value=\"+\" onclick=\"addRow('" + plus.tableId + "','" + plus.rowId + "','" + data_dynamicRowLabel + "');makeDirty();\" class=\"toolButton\"/>&#160;&#160;");
				}
			}
            //endregion
            //region Minus
			else if (className.equals("Minus")) {
				if(!forView) {
					Minus minus = (Minus) element;
					Container parent;
					// Retrieve closest table
					Table table = null;
					parent = minus.container;
					while (parent != null) {
						if (parent instanceof Table) {
							table = (Table) parent;
							break;
						} else {
							parent = parent.container;
						}
					}
					if (table != null) minus.tableId = table.id;
					tagBuilder.append("&#160;&#160;<input type=\"button\" value=\"-\" onclick=\"confirmDelete(&#39;" + minus.tableId + "&#39;,this);updateCalculatedFields();makeDirty();\" class=\"toolButton\"/>&#160;&#160;");
				}
			}
            //endregion
            //region PlusMinusBaDetails
			else if (className.equals("PlusMinusBaDetails")) {
				if(!forView) {
					PlusMinusBaDetails plusMinusBaDetails = (PlusMinusBaDetails) element;
					Container parent;
					// Retrieve closest table
					Table table = null;
					parent = plusMinusBaDetails.container;
					while (parent != null) {
						if (parent instanceof Table) {
							table = (Table) parent;
							break;
						} else {
							parent = parent.container;
						}
					}
					if (table != null) plusMinusBaDetails.tableId = table.id;
					// Retrieve closest row
					Tr tr = null;
					parent = plusMinusBaDetails.container;
					while (parent != null) {
						if (parent instanceof Tr) {
							tr = (Tr) parent;
							break;
						} else {
							parent = parent.container;
						}
					}
					if (tr != null && tr.id != null) {
						String groupId = tr.id.substring(tr.id.lastIndexOf(":") + 1);
						plusMinusBaDetails.rowId = groupId;
					}
					tagBuilder.append("<input type=\"button\" value=\"+\" onclick=\"updateHiddenValue([{name: 'tag', value: 'balanceAccountDetails'}, {name: 'tableId', value: '" + plusMinusBaDetails.tableId + "'}, {name: 'rowId', value: '" + plusMinusBaDetails.rowId + "'}]);makeDirty();\" class=\"toolButton\"/>");
					tagBuilder.append("<input type=\"button\" value=\"-\" onclick=\"delBalanceAccountDetails(this);calculate(this);makeDirty();\" class=\"toolButton\" disabled=\"disabled\"/>");
				}
			}
            //endregion
            //region Pick
            else if (className.equals("Pick")) {
				if(!forView) {
					Pick pick = (Pick) element;

					// Building receiver's compound name
					StringBuilder sbCompoundName = new StringBuilder();
					if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
					sbCompoundName.append("*");
					if (pick.receiver != null && !pick.receiver.isEmpty()) sbCompoundName.append(pick.receiver);
					sbCompoundName.append(":");
					if (pick.key != null && !pick.key.isEmpty()) sbCompoundName.append(pick.key);
					sbCompoundName.append(":");
					if (pick.keyValue != null && !pick.keyValue.isEmpty()) sbCompoundName.append(pick.keyValue);
					String receiverId = sbCompoundName.toString();

					String jsFunctionName = null;
					if (pick.ref != null) {
						if (pick.ref.equals("bank")) jsFunctionName = "pickBank";
						else if (pick.ref.equals("issuer")) jsFunctionName = "pickIssuer";
						else if (pick.ref.equals("security")) jsFunctionName = "pickSecurity";
						else if (pick.ref.equals("securityinfo")) jsFunctionName = "pickSecurityInfo";
						else if (pick.ref.equals("legalperson"))
							jsFunctionName = "pickLegalPerson"; // Deprecated, use combined pickPerson dialog instead.
						else if (pick.ref.equals("person"))
							jsFunctionName = "pickPerson"; // Combined picker dialog for legal and individual persons.
					}
					String onclickHandler = "";
					if (jsFunctionName != null) onclickHandler = jsFunctionName + "(&#39;" + receiverId + "&#39;);";

					tagBuilder.append("<input type=\"button\" ref=\"" + pick.ref + "\" value=\"...\" onclick=\"" + onclickHandler + "\" class=\"toolButton\"/>");
				}
			}
            //endregion
            //region Calculate
			else if (className.equals("Calculate")) {
				tagBuilder.append("<button type=\"button\" onclick=\"calculate(this);\" class=\"toolButton\"><span class=\"ui-icon ui-icon-calculator\"/></button>");
			}
            //endregion
            //region Style
            else if (className.equals("Style")) {
	            tagBuilder.append("\n<style>").append(templateStyle).append(((Style) element).content).append("</style>");
            }
            //endregion
            //region Script
            else if (className.equals("Script")) {
				Script script = ((Script) element);
				if(script.type!=null && script.type.equals("text/frsp")){
					tagBuilder.append("\n<script type=\"" + script.type + "\" >\n").append(script.content);
					tagBuilder.append("\n</script>\n");
				} else {
					jsCode = ((Script) element).content;
					tagBuilder.append("\n<script>\n");
					if(!forView){
						tagBuilder.append(templateScript).append(jsCode);

						fbAddCells.append("}");
						dynamicFunctions.add(fbAddCells.toString());
						for (String dynamicFunction : dynamicFunctions) tagBuilder.append(dynamicFunction);
					}else{
						tagBuilder.append("function updateCalculatedFields() {}");
					}
					tagBuilder.append("\n</script>\n");
				}
            }
            //endregion
            //region Script2
            else if (className.equals("Script2")) {
				tagBuilder.append("\n<script>\n").append(((Script2) element).content);
				tagBuilder.append("\n</script>\n");
			}
            //endregion
            //region ServerScript
			else if (className.equals("ServerScript")) {
				jsCode = (jsCode == null ? "" : jsCode + "\n\n") + ((ServerScript) element).content;
			}
			//endregion
		}
        return tagBuilder.toString();
    }

	private void registerInputValueCheck(InputValueCheck inputValueCheck, String dynamicRowLabel){
		if (dynamicRowLabel != null) {
			for (String groupId : dynamicLabelTrIds.get(dynamicRowLabel)) {
				InputValueCheck clone = new InputValueCheck(inputValueCheck);
				clone.setGroupId(inputValueCheck.getKey().substring(0, inputValueCheck.getKey().indexOf("$DynamicRowId")) + "$D." + groupId + ".");
				inputValueChecks.add(clone);
			}
		} else {
			inputValueChecks.add(inputValueCheck);
		}
	}

	private String getStyleAttributes(String style, String styleClass) {
		StringBuilder sb = new StringBuilder();
		if (style != null && !style.trim().isEmpty()) sb.append(" style=\"").append(style).append("\"");
		if (styleClass != null && !styleClass.trim().isEmpty()) sb.append(" class=\"").append(styleClass).append("\"");
		return sb.toString();
	}

    public String getHtmlWithForm() {
        if (form == null) {
            return "";
        } else {
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append("<form ").append(getStyleAttributes(form.style, form.styleClass)).append(">").append(html).append("</form>");
            return tagBuilder.toString();
        }
    }

    public String getHtmlWithFormAction(String formAction, String actionText) {
        if (form == null) {
            return "";
        } else {
            StringBuilder tagBuilder = new StringBuilder();
            tagBuilder.append("<form method=\"post\" action=\"").append(formAction).append("\"")
                    .append(getStyleAttributes(form.style, form.styleClass)).append(">").append(html)
                    .append("<br/><input type=\"submit\" value=\"").append(actionText).append("\">").append("</form>");
            return tagBuilder.toString();
        }
    }

	private void buildReceiverInputHtml(StringBuilder tagBuilder, String compoundName,
									String ref, String refCode, String refCaption,
									Boolean multiLine, Boolean multiValue, Boolean readonly, Boolean auto,
									String styleClass, String style, boolean forView){
		if (multiLine != null && multiLine) {
			tagBuilder.append("<textarea ");

			tagBuilder.append(" id=\"caption:" + compoundName + "\"");

			tagBuilder.append(" readonly=\"readonly\"");

			tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
			tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
			tagBuilder.append(" onkeyup=\"textAreaAdjust.call(this, this);\"");

			tagBuilder.append(" ref=\"" + ref + "\"");
			tagBuilder.append(" refCode=\"" + refCode + "\"");
			tagBuilder.append(" refCaption=\"" + refCaption + "\"");

			if (multiValue != null && multiValue.booleanValue()) tagBuilder.append(" multiValue=\"multiValue\"");

			styleClass = styleClass == null ? "receiverInput" : styleClass + " receiverInput";
			if ((readonly != null && readonly.booleanValue() == true) || forView) {
				styleClass += " readOnly";
			}

			if (auto != null && auto.booleanValue()) {
				styleClass = styleClass == null ? "auto" : styleClass + " auto";
			}
			style = style == null ? "resize: vertical;" : style + " resize: vertical;";
			styleClass = styleClass == null ? "autoHeight" : styleClass + " autoHeight";
			tagBuilder.append(getStyleAttributes(style, styleClass));

			tagBuilder.append(">");
			tagBuilder.append(" ");
			tagBuilder.append("</textarea>");
		} else {
			tagBuilder.append("<input type=\"text\"");

			tagBuilder.append(" id=\"caption:" + compoundName + "\"");
			tagBuilder.append(" readonly=\"readonly\"");

			tagBuilder.append(" ref=\"" + ref + "\"");
			tagBuilder.append(" refCode=\"" + refCode + "\"");
			tagBuilder.append(" refCaption=\"" + refCaption + "\"");

			if (multiValue != null && multiValue.booleanValue()) tagBuilder.append(" multiValue=\"multiValue\"");

			styleClass = styleClass == null ? "readOnly receiverInput" : styleClass + " readOnly receiverInput";
			if ((readonly != null && readonly.booleanValue() == true) || forView) {
				styleClass += " readOnly";
			}

			if (auto != null && auto.booleanValue()) {
				styleClass = styleClass == null ? "auto" : styleClass + " auto";
			}
			tagBuilder.append(getStyleAttributes(style, styleClass));

			tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
			tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
			if(!forView){
				tagBuilder.append(" onchange=\"makeDirty();\"");
			}
			tagBuilder.append("/>");
		}
		tagBuilder.append("<input type=\"hidden\"");
		tagBuilder.append(" id=\"" + compoundName + "\"");
		tagBuilder.append(" name=\"" + compoundName + "\"");
		if(!forView) {
			tagBuilder.append(" onchange=\"makeDirty();\"");
		}
		tagBuilder.append("/>");
	}

    // Getters and setters

    public String getHtml() {
        return html;
    }

	public List<InputValueCheck> getInputValueChecks() {
		return inputValueChecks;
	}

	public String getJsCode() {
		return jsCode;
	}
}
