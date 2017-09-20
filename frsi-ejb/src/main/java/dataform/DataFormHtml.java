package dataform;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import ejb.PeriodType;
import ejb.Reference;
import entities.*;
import jaxb.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import parser.DataType;
import util.Convert;
import util.PeriodUtil;

import javax.ejb.EJBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFormHtml extends DataForm {

	private static class ColGroupTempl {
		private String name;
		private Templ<HtmlCol> colTempl = new Templ<HtmlCol>();
		private Map<String, Templ<HtmlTh>> tHeadTempl = new HashMap<String, Templ<HtmlTh>>();
		private Map<String, Templ<HtmlTd>> tBodyTempl = new HashMap<String, Templ<HtmlTd>>();
		private Map<String, Templ<HtmlTh>> tFooterTempl = new HashMap<String, Templ<HtmlTh>>();

		public ColGroupTempl(String name) {
			this.name = name;
		}
	}

	private static class Templ<T extends HtmlTag> {
		private List<T> templates = new ArrayList<T>();
	}

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	private Gson gson = new Gson();

	private Map<String, List<InputSelectRecord>> singleRefs = new HashMap<String, List<InputSelectRecord>>();
	private Map<String, List<InputSelectRecord>> multiRefs = new HashMap<String, List<InputSelectRecord>>();
	private Map<String, Map<Long, InputSelectRecord>> refIdx = new HashMap<String, Map<Long, InputSelectRecord>>();
	private Map<String, Map<Long, LiteRefItem>> liteRefItems = new HashMap<String, Map<Long, LiteRefItem>>();

	private String html;
	private static final String FINISH_TAG = "<style";
	private Reference reference;
	private Date reportDate;
	private Date startDate = null;
	private Date endDate = null;
	private PeriodType periodType;
	private Map<String, InputSelectViewModel> inputSelectViewModels = new HashMap<String, InputSelectViewModel>();
	private Map<String, Map<String, SortedSet<Integer>>> colMinorIdsCache = new HashMap<String, Map<String, SortedSet<Integer>>>();

	private Map<String,Map<String, HtmlTr>> dynamicRowTemplates;
	private Map<String,Map<String, HtmlTBody>> dynamicGroupTemplates;
	private Map<String, Map<String, ColGroupTempl>> colGroupTemplates;

	private Pattern dynamicCellPattern = Pattern.compile("\\*[a-zA-Z0-9_]+@[0-9]{8}\\:");
	private InputSelectViewModel defaultViewModel;

	public DataFormHtml(String html, Map<String, String> attributeValueMap, Reference reference, Date reportDate) {
		super(attributeValueMap);
		this.html = html;
		this.reference = reference;
		this.reportDate = reportDate;
		insertValuesIntoHtml();
	}

	public DataFormHtml(String html, Map<String, String> attributeValueMap, Reference reference, Date reportDate, Date startDate, Date endDate, PeriodType periodType) {
		super(attributeValueMap);
		this.html = html;
		this.reference = reference;
		this.reportDate = reportDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.periodType = periodType;
		insertValuesIntoHtml();
	}

	private void insertValuesIntoHtml() {
		if (html == null || html.isEmpty()) return;
        try {
	        // Processing order is important!
			processFRSPScript();
			processRefs();
			processPickRefs();
			if (startDate != null && endDate != null && periodType != null) {
				processDynamicCells();
			}
			colGroupTemplates = getDynamicColGroupTemplates();
			html = processDynamicColGroups(html);
			dynamicRowTemplates = getDynamicRowTemplates();
			dynamicGroupTemplates = getDynamicGroupTemplates();
			html = processDynamicRows(html);
			html = processDynamicGroups(html);
			processInputTags();
			processTextareaTags();
			processSelectTags();
			processHiddenInputTags();
			processInputSelectTextareaTags();
			processReceiverInputTags();
			processReceiverInputTextAreaTags();
		} catch (JAXBException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}

	private void processDynamicCells() throws JAXBException{
		SimpleDateFormat dfParse = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat dfShow = new SimpleDateFormat("dd.MM.yyyy");
		SortedSet<Integer> cellKeys = new TreeSet<Integer>();
		DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
		LocalDate startDate = PeriodUtil.floor(new LocalDate(this.startDate, dtZone), periodType);
		LocalDate endDate = PeriodUtil.floor(new LocalDate(this.endDate, dtZone), periodType);
		LocalDate interDate = startDate;
		while (interDate.compareTo(endDate) <= 0) {
			cellKeys.add(Integer.parseInt(interDate.toString("yyyyMMdd")));
			interDate = PeriodUtil.plusPeriod(interDate, periodType, 1);
		}

		Pattern pCellNum = Pattern.compile("@DynamicCellNum:([0-9]+)");
		Matcher mCellNum;

		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag, closeTag;
		String openTag1 = "<th id=\"@DynamicCell\"";
		String closeTag1 = "</th>";
		String openTag2 = "<td id=\"@DynamicCell\"";
		String closeTag2 = "</td>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			openTag = openTag1;
			closeTag = closeTag1;
			startPos = html.indexOf(openTag, pos);
			if (startPos < 0) {
				openTag = openTag2;
				closeTag = closeTag2;
				startPos = html.indexOf(openTag, pos);
			}
			if (startPos >= 0) {
				endPos = html.indexOf(closeTag, startPos);
				if (endPos < 0) break;

				sb.append(html.substring(pos, startPos));
				pos = endPos + closeTag.length();

				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTag dynamicHtmlTag = (HtmlTag) unmarshaller.unmarshal(is);

					// Process
					if (dynamicHtmlTag != null) {
						dynamicHtmlTag.id = null;

						StringWriter stringWriter = new StringWriter();
						marshaller.marshal(dynamicHtmlTag, stringWriter);
						String strDynamicHtmlTdTemplate = stringWriter.toString();
						int index = 0;
						for (Integer cellKey : cellKeys) {
							Date date;
							try {
								date = dfParse.parse(Integer.toString(cellKey));
							} catch (ParseException e) {
								continue;
							}

							String strDynamicHtmlTd = strDynamicHtmlTdTemplate;
							strDynamicHtmlTd = strDynamicHtmlTd.replace("@DynamicCellId", "@" + Integer.toString(cellKey));
							strDynamicHtmlTd = strDynamicHtmlTd.replace("@DynamicCellDate", dfShow.format(date));
							mCellNum = pCellNum.matcher(strDynamicHtmlTd);
							if(mCellNum.find()){
								String group = mCellNum.group();
								int startIndex;
								try {
									startIndex = Integer.parseInt(group.substring(group.indexOf(":") + 1));
								} catch (NumberFormatException e) {
									startIndex = 1;
								}
								strDynamicHtmlTd = mCellNum.replaceAll(Integer.toString(startIndex + index));
							}
							sb.append(strDynamicHtmlTd);
							index++;
						}
					}
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	// Returns dynamic row templates per table and removes templates from html. Map keys contain table names
	private Map<String, Map<String, HtmlTr>> getDynamicRowTemplates() throws JAXBException {
		Map<String,Map<String, HtmlTr>> result = new HashMap<String, Map<String, HtmlTr>>();
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos, dynPos;
		String openTag = "<tr";
		String closeTag = "</tr>";
		String dynTag = "$DynamicRowIdMinor";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			dynPos = html.indexOf(dynTag, pos);
			if (dynPos < 0 || dynPos > finishPos) break;

			startPos = html.lastIndexOf(openTag, dynPos);
			endPos = html.indexOf(closeTag, dynPos);

			if (startPos < 0 || endPos < 0) break;
			sb.append(html.substring(pos, startPos));
			pos = endPos + closeTag.length();

			String srcHtmlTag = html.substring(startPos, pos);
			InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
			HtmlTr htmlTr = (HtmlTr) unmarshaller.unmarshal(is);

			if (htmlTr != null && htmlTr.id != null) {
				String[] idParts = htmlTr.id.split(":");
				if (idParts.length > 0) {
					String tableName = idParts[0];
					String dynamicRowLabel = htmlTr.data_dynamicRowLabel == null ? "null" : htmlTr.data_dynamicRowLabel;
					if(!result.containsKey(tableName))
						result.put(tableName, new HashMap<String, HtmlTr>());
					result.get(tableName).put(dynamicRowLabel, htmlTr);
				}
			}
		}
		sb.append(html.substring(pos));
		html = sb.toString();
		return result;
	}

	private Map<String, Map<String, HtmlTBody>> getDynamicGroupTemplates() throws JAXBException {
		Map<String,Map<String, HtmlTBody>> result = new HashMap<String, Map<String, HtmlTBody>>();
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTBody.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos, dynPos;
		String openTag = "<tbody";
		String closeTag = "</tbody>";
		String dynTag = "$DynamicGroupIdMinor";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			dynPos = html.indexOf(dynTag, pos);
			if (dynPos < 0 || dynPos > finishPos) break;

			startPos = html.lastIndexOf(openTag, dynPos);
			endPos = html.indexOf(closeTag, dynPos);

			if (startPos < 0 || endPos < 0) break;
			sb.append(html.substring(pos, startPos));
			pos = endPos + closeTag.length();

			String srcHtmlTag = html.substring(startPos, pos);
			InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
			HtmlTBody htmlTBody = (HtmlTBody) unmarshaller.unmarshal(is);

			if (htmlTBody != null && htmlTBody.id != null) {
				String[] idParts = htmlTBody.id.split(":");
				if (idParts.length > 0) {
					String tableName = idParts[0];
					String dynamicRowLabel = htmlTBody.data_dynamicGroupLabel == null ? "null" : htmlTBody.data_dynamicGroupLabel;
					if (!result.containsKey(tableName))
						result.put(tableName, new HashMap<String, HtmlTBody>());
					result.get(tableName).put(dynamicRowLabel, htmlTBody);
				}
			}
		}
		sb.append(html.substring(pos));
		html = sb.toString();
		return result;
	}

	private Map<String, Map<String, ColGroupTempl>> getDynamicColGroupTemplates() throws JAXBException {
		Map<String, Map<String, ColGroupTempl>> result = new HashMap<String, Map<String, ColGroupTempl>>();

		fillColGroupTemplates("<col ", "/>", HtmlCol.class, result);
		fillColGroupTemplates("<th ", "</th>", HtmlTh.class, result);
		fillColGroupTemplates("<td ", "</td>", HtmlTd.class, result);

		return result;
	}

	private <T extends HtmlTag> void fillColGroupTemplates(String openTag, String closeTag, Class<T> clazz, Map<String, Map<String, ColGroupTempl>> result) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos, dynPos;
		String dynTag = "data-templ";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			dynPos = html.indexOf(dynTag, pos);
			if (dynPos < 0 || dynPos > finishPos) break;

			startPos = html.lastIndexOf(openTag, dynPos);
			if (html.lastIndexOf("<", dynPos) > startPos)
				break;
			endPos = html.indexOf(closeTag, dynPos);

			if (startPos < 0 || endPos < 0) break;
			sb.append(html.substring(pos, startPos));
			pos = endPos + closeTag.length();

			String srcHtmlTag = html.substring(startPos, pos);
			InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
			T htmlTag;
			try {
				htmlTag = clazz.cast(unmarshaller.unmarshal(is));
			} catch (ClassCastException e){
				throw new IllegalStateException(MessageFormat.format("Can't cast object to {0}", clazz.getName()));
			}

			if (htmlTag != null) {
				String dataTempl;
				if (htmlTag instanceof HtmlCol)
					dataTempl = ((HtmlCol) htmlTag).data_templ;
				else if (htmlTag instanceof HtmlTd)
					dataTempl = ((HtmlTd) htmlTag).data_templ;
				else if (htmlTag instanceof HtmlTh)
					dataTempl = ((HtmlTh) htmlTag).data_templ;
				else
					throw new IllegalStateException("System exception in getColTemplates method");
				String[] idParts = dataTempl.split(":");
				if (idParts.length == 3) {
					String tableName = idParts[0];
					String dynamicGroup = idParts[1];
					String rowId = idParts[2];
					if (!result.containsKey(tableName))
						result.put(tableName, new HashMap<String, ColGroupTempl>());
					Map<String, ColGroupTempl> tableTemplates = result.get(tableName);
					ColGroupTempl groupTempl = tableTemplates.get(dynamicGroup);
					if(groupTempl==null){
						groupTempl = new ColGroupTempl(dynamicGroup);
						tableTemplates.put(groupTempl.name, groupTempl);
					}
					if (htmlTag instanceof HtmlCol){
						groupTempl.colTempl.templates.add((HtmlCol) htmlTag);
					} else if (htmlTag instanceof HtmlTd){
						Templ<HtmlTd> templ = groupTempl.tBodyTempl.get(rowId);
						if(templ==null){
							templ = new Templ<HtmlTd>();
							groupTempl.tBodyTempl.put(rowId, templ);
						}
						templ.templates.add((HtmlTd) htmlTag);
					} else if (htmlTag instanceof HtmlTh){
						if (rowId.startsWith("$head")) {
							Templ<HtmlTh> templ = groupTempl.tHeadTempl.get(rowId);
							if (templ == null) {
								templ = new Templ<HtmlTh>();
								groupTempl.tHeadTempl.put(rowId, templ);
							}
							templ.templates.add((HtmlTh) htmlTag);
						}
						if (rowId.startsWith("$foot")) {
							Templ<HtmlTh> templ = groupTempl.tFooterTempl.get(rowId);
							if (templ == null) {
								templ = new Templ<HtmlTh>();
								groupTempl.tFooterTempl.put(rowId, templ);
							}
							templ.templates.add((HtmlTh) htmlTag);
						}
					}
				}
			}
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private String processDynamicRows(String html) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<tr id=";
		String closeTag = "</tr>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				endPos = html.indexOf(closeTag, startPos);
				if (endPos < 0) break;

				// Skip outer tags if tags nested, i.e. set startPos to the inner openTag which is closest to the closeTag
				int nestedTagPos = -1;
				do {
					nestedTagPos = html.indexOf(openTag, startPos + openTag.length());
					if (nestedTagPos > endPos) nestedTagPos = -1;
					if (nestedTagPos >= 0) startPos = nestedTagPos;
				} while (nestedTagPos >= 0);

				sb.append(html.substring(pos, startPos));
				pos = endPos + closeTag.length();

				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					sb.append(srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTr groupHtmlTr = (HtmlTr) unmarshaller.unmarshal(is);

					// Process
					if (groupHtmlTr != null && groupHtmlTr.id != null && groupHtmlTr.data_dynamicRowLabel != null) {
						String[] idParts = groupHtmlTr.id.split(":");
						if (idParts.length > 1) {
							String tableName = idParts[0];
							String groupId = idParts[1];
							String dynamicRowLabel = groupHtmlTr.data_dynamicRowLabel == null ? "null" : groupHtmlTr.data_dynamicRowLabel;
							HtmlTr templateRow = dynamicRowTemplates.get(tableName).get(dynamicRowLabel);
							List<String> dynamicRowsHtml = getDynamicRowsHtml(tableName, groupId, templateRow);
							for (String dynamicRowHtml : dynamicRowsHtml) sb.append(dynamicRowHtml);
						}
					}
				}
			} else break;
		}
		sb.append(html.substring(pos));
		return sb.toString();
	}

	private String processDynamicGroups(String html) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<tr id=";
		String closeTag = "</tr>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				endPos = html.indexOf(closeTag, startPos);
				if (endPos < 0) break;

				// Skip outer tags if tags nested, i.e. set startPos to the inner openTag which is closest to the closeTag
				int nestedTagPos = -1;
				do {
					nestedTagPos = html.indexOf(openTag, startPos + openTag.length());
					if (nestedTagPos > endPos) nestedTagPos = -1;
					if (nestedTagPos >= 0) startPos = nestedTagPos;
				} while (nestedTagPos >= 0);

				sb.append(html.substring(pos, startPos));
				pos = endPos + closeTag.length();

				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					sb.append(srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTr groupHtmlTr = (HtmlTr) unmarshaller.unmarshal(is);

					// Process
					if (groupHtmlTr != null && groupHtmlTr.id != null && groupHtmlTr.data_dynamicGroupLabel != null) {
						String[] idParts = groupHtmlTr.id.split(":");
						if (idParts.length > 1) {
							String tableName = idParts[0];
							String groupId = idParts[1];
							String dynamicGroupLabel = groupHtmlTr.data_dynamicGroupLabel;
							HtmlTBody templateGroup = dynamicGroupTemplates.get(tableName).get(dynamicGroupLabel);
							List<String> dynamicRowsHtml = getDynamicGroupsHtml(tableName, groupId, templateGroup);
							for (String dynamicRowHtml : dynamicRowsHtml) {
								dynamicRowHtml = processDynamicRows(dynamicRowHtml);
								sb.append(dynamicRowHtml);
							}
						}
					}
				}
			} else break;
		}
		sb.append(html.substring(pos));
		return sb.toString();
	}

	private String processDynamicColGroups(String html) throws JAXBException {
		html = processDynamicColGroupsByTag(html, "<col id=", "/>", HtmlCol.class);
		html = processDynamicColGroupsByTag(html, "<th id=", "</th>", HtmlTh.class);
		html = processDynamicColGroupsByTag(html, "<td id=", "</td>", HtmlTd.class);
		return html;
	}

	private <T extends HtmlTag> String processDynamicColGroupsByTag(String html, String openTag, String closeTag, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				endPos = html.indexOf(closeTag, startPos);
				if (endPos < 0) break;

				// Skip outer tags if tags nested, i.e. set startPos to the inner openTag which is closest to the closeTag
				int nestedTagPos = -1;
				do {
					nestedTagPos = html.indexOf(openTag, startPos + openTag.length());
					if (nestedTagPos > endPos) nestedTagPos = -1;
					if (nestedTagPos >= 0) startPos = nestedTagPos;
				} while (nestedTagPos >= 0);

				sb.append(html.substring(pos, startPos));
				if (closeTag.equals("/>")) {
					pos = endPos + 2;
				} else {
					pos = endPos + closeTag.length();
				}

				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					sb.append(srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					T groupHtmlTag;
					try {
						groupHtmlTag = clazz.cast(unmarshaller.unmarshal(is));
					} catch (ClassCastException e){
						throw new IllegalStateException(MessageFormat.format("Can't cast object to {0}", clazz.getName()));
					}

					// Process
					String dynamicGroupLabel;
					if (groupHtmlTag instanceof HtmlCol)
						dynamicGroupLabel = ((HtmlCol) groupHtmlTag).data_dynamicGroupLabel;
					else if (groupHtmlTag instanceof HtmlTd)
						dynamicGroupLabel = ((HtmlTd) groupHtmlTag).data_dynamicGroupLabel;
					else if (groupHtmlTag instanceof HtmlTh)
						dynamicGroupLabel = ((HtmlTh) groupHtmlTag).data_dynamicGroupLabel;
					else
						throw new IllegalStateException("Can't cast groupHtmlTag");

					if (groupHtmlTag.id != null && dynamicGroupLabel != null) {
						String[] idParts = groupHtmlTag.id.split(":");
						if (idParts.length == 3) {
							String tableName = idParts[0];
							String groupId = idParts[1];
							String rowId = idParts[2];
							String tRowId = dynamicGroupLabel.substring(dynamicGroupLabel.lastIndexOf(":") + 1);

							String tmplName = dynamicGroupLabel.substring(dynamicGroupLabel.indexOf(":") + 1, dynamicGroupLabel.lastIndexOf(":"));

							Map<String, ColGroupTempl> tableTmpls = colGroupTemplates.get(tableName);
							if (tableTmpls == null)
								throw new IllegalStateException("Can't find template " + dynamicGroupLabel);
							ColGroupTempl tmpl = tableTmpls.get(tmplName);
							if (tmpl == null)
								throw new IllegalStateException("Can't find template " + dynamicGroupLabel);

							List<String> dynamicRowsHtml;
							if (groupHtmlTag instanceof HtmlCol) {
								HtmlCol col = (HtmlCol) groupHtmlTag;
								dynamicRowsHtml = getDynamicColGroupsHtml(tableName, groupId, col.data_cellText, rowId, tmpl.colTempl, HtmlCol.class);
							} else if (groupHtmlTag instanceof HtmlTd) {
								HtmlTd td = (HtmlTd) groupHtmlTag;
								dynamicRowsHtml = getDynamicColGroupsHtml(tableName, groupId, td.data_cellText, rowId, tmpl.tBodyTempl.get(tRowId), HtmlTd.class);
							} else if (groupHtmlTag instanceof HtmlTh) {
								HtmlTh th = (HtmlTh) groupHtmlTag;
								if (tRowId.startsWith("$head")) {
									dynamicRowsHtml = getDynamicColGroupsHtml(tableName, groupId, th.data_cellText, rowId, tmpl.tHeadTempl.get(tRowId), HtmlTh.class);
								} else if (tRowId.startsWith("$foot")) {
									dynamicRowsHtml = getDynamicColGroupsHtml(tableName, groupId, th.data_cellText, rowId, tmpl.tFooterTempl.get(tRowId), HtmlTh.class);
								} else {
									throw new IllegalStateException("Incorrect dynamic col group label");
								}
							} else {
								throw new IllegalStateException();
							}
							for (String dynamicRowHtml : dynamicRowsHtml) {
								dynamicRowHtml = processDynamicRows(dynamicRowHtml);
								sb.append(dynamicRowHtml);
							}
						}
					}
				}
			} else break;
		}
		sb.append(html.substring(pos));
		return sb.toString();
	}

	private List<String> getDynamicRowsHtml(String tableName, String groupId, HtmlTr templateRow) throws JAXBException {
		List<String> result = new ArrayList<String>();
		String tablePrefix = tableName + ":";
		String groupPrefix;
		if (groupId.contains("$D.")) {
			groupPrefix = groupId + ".";
		} else {
			groupPrefix = "$D." + groupId + ".";
		}

		SortedSet<Integer> minorIds = new TreeSet<Integer>();
		for (Map.Entry<String,String> entry : attributeValueMap.entrySet()) {
            String[] keyPartsByAsterisk = entry.getKey().split("\\*");
			String[] keyPartsByColon = entry.getKey().split(":");
			if (keyPartsByAsterisk.length > 0 && keyPartsByColon.length > 2) {
                String entryTableName = keyPartsByAsterisk[0];
				String entryRowId = keyPartsByColon[2];
				if (entryRowId.startsWith(groupPrefix) && entryTableName.equals(tableName)) {
					String lastIdPartByDot = entryRowId.substring(groupPrefix.length());
					if (lastIdPartByDot.contains(".")) {
						lastIdPartByDot = lastIdPartByDot.substring(0, lastIdPartByDot.indexOf("."));
					}
                    Integer minorId = null;
                    try { minorId = Integer.valueOf(lastIdPartByDot); } catch (NumberFormatException e) {}
                    if (minorId != null) minorIds.add(minorId);
				}
			}
		}

		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

		for (Integer minorId : minorIds) {
			String dynamicRowId = groupPrefix + minorId.intValue();
			String dynamicRowIdText = String.valueOf(minorId.intValue());
			if (!groupId.equals("group")) dynamicRowIdText = groupId + "." + dynamicRowIdText;
			dynamicRowIdText = dynamicRowIdText.replace("$D.","");

            HtmlTr htmlTr = new HtmlTr();
			htmlTr.id = tablePrefix + dynamicRowId;
			htmlTr.ths = templateRow.ths;
			htmlTr.tds = templateRow.tds;

			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(htmlTr, stringWriter);
			String strHtmlTr = stringWriter.toString();

			result.add(strHtmlTr.replace("$DynamicRowIdText", dynamicRowIdText).replace("$DynamicRowId", dynamicRowId));
		}
		return result;
	}

	private List<String> getDynamicGroupsHtml(String tableName, String groupId, HtmlTBody templateGroup) throws JAXBException {
		List<String> result = new ArrayList<String>();
		String tablePrefix = tableName + ":";
		String groupPrefix;
		if (groupId.contains("$D.")) {
			groupPrefix = groupId + ".";
		} else {
			groupPrefix = "$D." + groupId + ".";
		}

		SortedSet<Integer> minorIds = new TreeSet<Integer>();
		for (Map.Entry<String,String> entry : attributeValueMap.entrySet()) {
			String[] keyPartsByAsterisk = entry.getKey().split("\\*");
			String[] keyPartsByColon = entry.getKey().split(":");
			if (keyPartsByAsterisk.length > 0 && keyPartsByColon.length > 2) {
				String entryTableName = keyPartsByAsterisk[0];
				String entryRowId = keyPartsByColon[2];
				if (entryRowId.startsWith(groupPrefix) && entryTableName.equals(tableName)) {
					String lastIdPartByDot = entryRowId.substring(groupPrefix.length());
					if (lastIdPartByDot.contains(".")) {
						lastIdPartByDot = lastIdPartByDot.substring(0, lastIdPartByDot.indexOf("."));
					}
					Integer minorId = null;
					try { minorId = Integer.valueOf(lastIdPartByDot); } catch (NumberFormatException e) {}
					if (minorId != null) minorIds.add(minorId);
				}
			}
		}

		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

		for (Integer minorId : minorIds) {
			for (HtmlTr templateRow : templateGroup.trs) {
				String dynamicRowId = groupPrefix + minorId.intValue() + ((templateRow.data_rowId == null || templateRow.data_rowId.isEmpty()) ? "" : ("." + templateRow.data_rowId));
				String dynamicRowIdText = String.valueOf(minorId.intValue());
				if (!groupId.equals("group")) dynamicRowIdText = groupId + "." + dynamicRowIdText;
				if (templateRow.data_rowId != null && !templateRow.data_rowId.isEmpty()) {
					dynamicRowIdText += "." + templateRow.data_rowId;
				}

				HtmlTr htmlTr = new HtmlTr();
				htmlTr.id = tablePrefix + dynamicRowId;
				htmlTr.ths = templateRow.ths;
				htmlTr.tds = templateRow.tds;
				htmlTr.data_dynamicRowLabel = templateRow.data_dynamicRowLabel;
				htmlTr.data_dynamicGroupLabel = templateRow.data_dynamicGroupLabel;

				StringWriter stringWriter = new StringWriter();
				marshaller.marshal(htmlTr, stringWriter);
				String strHtmlTr = stringWriter.toString();

				result.add(strHtmlTr.replace("$DynamicRowIdText", dynamicRowIdText).replace("$DynamicRowId", dynamicRowId));
			}
		}
		return result;
	}

	private <T extends HtmlTag> List<String> getDynamicColGroupsHtml(String tableName, String groupId, String parentCellText, String rowId, Templ<T> tmpl, Class<T> clazz) throws JAXBException {
		if (tmpl.templates.size() == 0)
			return new ArrayList<String>();

		List<String> result = new ArrayList<String>();
		String tablePrefix = tableName + ":";
		String groupPrefix;
		if (groupId.contains("$C.")) {
			groupPrefix = groupId + ".";
		} else {
			groupPrefix = "$C." + groupId + ".";
		}

		SortedSet<Integer> minorIds;
		if (!colMinorIdsCache.containsKey(tableName) || !colMinorIdsCache.get(tableName).containsKey(groupPrefix)) {

			minorIds = new TreeSet<Integer>();
			for (Map.Entry<String, String> entry : attributeValueMap.entrySet()) {
				String[] keyPartsByAsterisk = entry.getKey().split("\\*");
				String[] keyPartsByColon = entry.getKey().split(":");
				if (keyPartsByAsterisk.length > 0 && keyPartsByColon.length > 2) {
					String entryTableName = keyPartsByAsterisk[0];
					String entryColName = keyPartsByAsterisk[1].substring(0, keyPartsByAsterisk[1].indexOf(":"));
					if (entryColName.startsWith(groupPrefix) && entryTableName.equals(tableName)) {
						String lastIdPartByDot = entryColName.substring(groupPrefix.length());
						if (lastIdPartByDot.contains(".")) {
							lastIdPartByDot = lastIdPartByDot.substring(0, lastIdPartByDot.indexOf("."));
						}
						Integer minorId = null;
						try {
							minorId = Integer.valueOf(lastIdPartByDot);
						} catch (NumberFormatException e) {
						}
						if (minorId != null) minorIds.add(minorId);
					}
				}
			}

			colMinorIdsCache.put(tableName, new HashMap<String, SortedSet<Integer>>());
			colMinorIdsCache.get(tableName).put(groupPrefix, minorIds);
		} else {
			minorIds = colMinorIdsCache.get(tableName).get(groupPrefix);
		}

		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

		for (Integer minorId : minorIds) {
			boolean hasDataColspan = false;
			for (T templateTag : tmpl.templates) {

				String dataCellId = null;
				String dataCellText = null;
				if (templateTag instanceof HtmlCol) {
					HtmlCol col = (HtmlCol) templateTag;
					dataCellId = col.data_cellId;
					dataCellText = col.data_cellText;
				} else if (templateTag instanceof HtmlTh) {
					HtmlTh th = (HtmlTh) templateTag;
					dataCellId = th.data_cellId;
					dataCellText = th.data_cellText;
				} else if (templateTag instanceof HtmlTd) {
					HtmlTd td = (HtmlTd) templateTag;
					dataCellId = td.data_cellId;
					dataCellText = td.data_cellText;
				}

				if (dataCellId == null) dataCellId = "";
				if (dataCellText == null) dataCellText = "";

				String dynamicColName = groupPrefix + minorId + (dataCellId.isEmpty() ? "" : ("." + dataCellId));
				String dynamicColNameText = String.valueOf(minorId.intValue());
//				if (!groupId.equals("group")) dynamicColNameText = groupId + "." + dynamicColNameText;
				if (parentCellText != null && !parentCellText.isEmpty())
					dynamicColNameText = parentCellText + "." + dynamicColNameText;
				if (!dataCellText.isEmpty()) {
					dynamicColNameText += "." + dataCellText;
				} else if (!dataCellId.isEmpty()) {
					dynamicColNameText += "." + dataCellId;
				}

				StringWriter stringWriter = new StringWriter();
				String id = tablePrefix + dynamicColName + ":" + rowId;
				if (templateTag instanceof HtmlCol) {
					HtmlCol t = (HtmlCol) templateTag;

					HtmlCol htmlCol = new HtmlCol(t);
					htmlCol.id = id;
					htmlCol.data_templ = null;

					marshaller.marshal(htmlCol, stringWriter);
				} else if (templateTag instanceof HtmlTh) {
					HtmlTh t = (HtmlTh) templateTag;

					HtmlTh htmlTh = new HtmlTh(t);
					htmlTh.id = id;
					htmlTh.data_templ = null;
					if (t.data_colspan != null) {
						htmlTh.colspan = String.valueOf(minorIds.size());
						hasDataColspan = true;
					}

					marshaller.marshal(htmlTh, stringWriter);
				} else if (templateTag instanceof HtmlTd) {
					HtmlTd t = (HtmlTd) templateTag;

					HtmlTd htmlTd = new HtmlTd(t);
					htmlTd.id = id;
					htmlTd.data_templ = null;

					marshaller.marshal(htmlTd, stringWriter);
				}
				String strHtmlTag = stringWriter.toString();
				strHtmlTag = strHtmlTag.replace("$DynamicColNameText", dynamicColNameText).replace("$DynamicColName", dynamicColName);
				result.add(strHtmlTag);
			}
			if (hasDataColspan) break;
		}

		return result;
	}

	private void processInputTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlInput.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<input";
		String closeTag = "</input>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();

		List<String> pickRefs = new ArrayList<String>();

		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf("/>", startPos); // non-container tag, i.e. can be self-closing
				if (endPos >= 0) {
					pos = endPos + 2;
				} else {
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
				}
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlInput htmlInput = (HtmlInput) unmarshaller.unmarshal(is);

					if (isReceiverCaption(htmlInput)) {
						sb.append(srcHtmlTag);
						continue;
					}

					// Process
					if (htmlInput.type.toLowerCase().equals("text")) {
							/*
							String[] nameParts = htmlInput.name.split("\\*");
							String containerName = nameParts[0];
							if (containerName.contains(":")) {
								containerName = containerName.replace(":", "_") + "_item";
							}
							String key = containerName + "*" + nameParts[1];
							*/
						String value = attributeValueMap.get(htmlInput.name);
						htmlInput.value = value == null ? null : value;
						if (isDynamicCell(htmlInput)) {
							if (value == null) {
								removeMaskMoney(htmlInput);
							}
						}

						if (value != null) {
							if (value.equals("NaN")) {
								htmlInput.value = "ОШИБКА";
								removeMaskMoney(htmlInput);
							}
						}

						StringWriter stringWriter = new StringWriter();
						marshaller.marshal(htmlInput, stringWriter);
						String dstHtmlTag = stringWriter.toString();
						//logger.debug("dst = " + dstHtmlTag);
						sb.append(dstHtmlTag);
					} else
						sb.append(srcHtmlTag);

				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void removeMaskMoney(HtmlTag htmlInput) {
		if (htmlInput.clazz != null) {
			htmlInput.clazz = htmlInput.clazz.replaceAll("maskMoney[0-9]", "");
		}
	}

	private boolean isDynamicCell(HtmlInput htmlInput) {
		if (htmlInput.name == null) {
			return false;
		} else {
			Matcher m = dynamicCellPattern.matcher(htmlInput.name);
			return m.find();
		}
	}

	private void processTextareaTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTextarea.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<textarea";
		String closeTag = "</textarea>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;

				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTextarea htmlTextarea = (HtmlTextarea) unmarshaller.unmarshal(is);

					if (isInputSelectTextArea(htmlTextarea) || isReceiverCaption(htmlTextarea)) {
						sb.append(srcHtmlTag);
						continue;
					}

					// Process
					String value = attributeValueMap.get(htmlTextarea.name);
					htmlTextarea.value = value == null ? " " : value;

					StringWriter stringWriter = new StringWriter();
					marshaller.marshal(htmlTextarea, stringWriter);
					String dstHtmlTag = stringWriter.toString();
					//logger.debug("dst = " + dstHtmlTag);
					sb.append(dstHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void processSelectTags() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlSelect.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<select";
		String closeTag = "</select>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlSelect htmlSelect = (HtmlSelect) unmarshaller.unmarshal(is);

					// Process
					String value = attributeValueMap.get(htmlSelect.name);
					for (HtmlOption htmlOption : htmlSelect.options)
						if (htmlOption.value != null && htmlOption.value.equals(value)) htmlOption.selected = "selected";

					StringWriter stringWriter = new StringWriter();
					marshaller.marshal(htmlSelect, stringWriter);
					String dstHtmlTag = stringWriter.toString();
					//logger.debug("dst = " + dstHtmlTag);
					sb.append(dstHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void processHiddenInputTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlInput.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<input";
		String closeTag = "</input>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf("/>", startPos); // non-container tag, i.e. can be self-closing
				if (endPos >= 0) {
					pos = endPos + 2;
				} else {
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
				}
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlInput htmlInput = (HtmlInput) unmarshaller.unmarshal(is);

					// Process
					if (htmlInput.type.toLowerCase().equals("hidden")) {
						String value = attributeValueMap.get(htmlInput.name);
						htmlInput.value = value == null ? null : value;

						StringWriter stringWriter = new StringWriter();
						marshaller.marshal(htmlInput, stringWriter);
						String dstHtmlTag = stringWriter.toString();
						//logger.debug("dst = " + dstHtmlTag);
						sb.append(dstHtmlTag);
					} else
						sb.append(srcHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void processFRSPScript() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTextarea.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<script type=\"text/frsp\"";
		String closeTag = "</script>";
		pos = 0;
		finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					String s = srcHtmlTag.substring(srcHtmlTag.indexOf(">") + 1, srcHtmlTag.indexOf("</script>"));
					try {
						FRSPScript frspScript = gson.fromJson(s, FRSPScript.class);
						if (frspScript != null && frspScript.getInputSelectViewModels() != null) {
							for (InputSelectViewModel vm : frspScript.getInputSelectViewModels()) {
								if (vm.getColumns() != null) { // для старых шаблонов 12.04.2017
									for (InputSelectColumn col : vm.getColumns()) {
										if (col.getValueType() == null) {
											col.setValueType(ValueType.STRING);
										}
									}
								}
								inputSelectViewModels.put(vm.getName(), vm);
							}
						}
					} catch (JsonSyntaxException e){
						e.printStackTrace();
					}

				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void processInputSelectTextareaTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTextarea.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<textarea";
		String closeTag = "</textarea>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTextarea htmlTextarea = (HtmlTextarea) unmarshaller.unmarshal(is);

					if (!isInputSelectTextArea(htmlTextarea)) {
						sb.append(srcHtmlTag);
						continue;
					}

					// Process
					String htmlTextareaIdPrefix = "textarea:";
					String value = attributeValueMap.get(htmlTextarea.id.substring(htmlTextareaIdPrefix.length()));
					if (value != null && htmlTextarea.multiValue != null && htmlTextarea.multiValue.toLowerCase().equals("multivalue"))
						htmlTextarea.value = getMultiValueCaption(value, htmlTextarea.ref, htmlTextarea.refCaption);
					else if (value != null) {
						try {
							Long recId = Long.parseLong(value);
							Map<Long, InputSelectRecord> idx = refIdx.get(htmlTextarea.ref);
							if (idx.containsKey(recId)) {
								try {
									htmlTextarea.value = DataType.variantToString(idx.get(recId).get(htmlTextarea.refCaption), ValueType.STRING);
								} catch (Exception e){
									throw new IllegalStateException(e.getMessage());
								}
							} else
								htmlTextarea.value = "не найден в справочнике";
						} catch (NumberFormatException e) {
							htmlTextarea.value = value;
						}
						if (htmlTextarea.value == null || htmlTextarea.value.isEmpty()) {
							htmlTextarea.value = " ";
						}
					}

					StringWriter stringWriter = new StringWriter();
					marshaller.marshal(htmlTextarea, stringWriter);
					String dstHtmlTag = stringWriter.toString();
					//logger.debug("dst = " + dstHtmlTag);
					sb.append(dstHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private String getMultiValueCaption(String value, String ref, String refCaption){
		try {
			MultiSelectValue parsedValue = gson.fromJson(value, MultiSelectValue.class);
			if (parsedValue != null && parsedValue.getValues() != null) {
				Map<Long, InputSelectRecord> idx = refIdx.get(ref);
				StringBuilder sb2 = new StringBuilder();
				for (int i = 0; i < parsedValue.getValues().size(); i++) {
					String code = parsedValue.getValues().get(i);
					try {
						Long recId = Long.parseLong(code);
						if (idx.containsKey(recId)) {
							try {
								code = DataType.variantToString(idx.get(recId).get(refCaption), ValueType.STRING);
							} catch (Exception e) {
								throw new IllegalStateException(e.getMessage());
							}
						} else {
							code = "-1";
						}
					} catch (NumberFormatException e) {
						code = "-1";
					}
					code = code.equals("-1") ? "Выберите данные из справочника" : code;
					if (code != null && !code.isEmpty()) {
						if (sb2.toString().length() > 0) sb2.append(", ");
						sb2.append(code);
					}
				}
				return sb2.toString();
			} else {
				return " ";
			}
		} catch (JsonSyntaxException e) {
			return value;
		}
	}

	private void processReceiverInputTextAreaTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTextarea.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<textarea";
		String closeTag = "</textarea>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTextarea htmlTextarea = (HtmlTextarea) unmarshaller.unmarshal(is);

					if (!isReceiverCaption(htmlTextarea)) {
						sb.append(srcHtmlTag);
						continue;
					}

					// Process
					String htmlTextareaIdPrefix = "caption:";
					String value = attributeValueMap.get(htmlTextarea.id.substring(htmlTextareaIdPrefix.length()));
					if (value != null && htmlTextarea.multiValue != null && htmlTextarea.multiValue.toLowerCase().equals("multivalue")) {
						htmlTextarea.value = getMultiValueCaption(value, htmlTextarea.ref, htmlTextarea.refCaption);
					} else if (value != null && !value.isEmpty()) {
						try {
							Long recId = Long.parseLong(value);
							if (recId == -1)
								htmlTextarea.value = "не найден в справочнике";
							else
								htmlTextarea.value = getRefCaptionByRecId(htmlTextarea.ref, htmlTextarea.refCaption, recId);
							if (htmlTextarea.value == null || htmlTextarea.value.isEmpty()) {
								htmlTextarea.value = " ";
							}
						} catch (NumberFormatException e) {
							htmlTextarea.value = "не найден в справочнике";
						}
					}

					StringWriter stringWriter = new StringWriter();
					marshaller.marshal(htmlTextarea, stringWriter);
					String dstHtmlTag = stringWriter.toString();
					//logger.debug("dst = " + dstHtmlTag);
					sb.append(dstHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private void processReceiverInputTags() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlInput.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		StringBuilder sb = new StringBuilder();
		int pos, startPos, endPos, finishPos;
		String openTag = "<input";
		String closeTag = "</input>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				sb.append(html.substring(pos, startPos));
				endPos = html.indexOf("/>", startPos); // non-container tag, i.e. can be self-closing
				if (endPos >= 0) {
					pos = endPos + 2;
				} else {
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
				}
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlInput htmlInput = (HtmlInput) unmarshaller.unmarshal(is);

					if (!isReceiverCaption(htmlInput)) {
						sb.append(srcHtmlTag);
						continue;
					}

					// Process
					String htmlInputIdPrefix = "caption:";
					String value = attributeValueMap.get(htmlInput.id.substring(htmlInputIdPrefix.length()));
					if (value != null && !value.isEmpty()) {
						try {
							Long recId = Long.parseLong(value);
							if (recId == -1)
								htmlInput.value = "не найден в справочнике";
							else
							htmlInput.value = getRefCaptionByRecId(htmlInput.ref, htmlInput.refCaption, recId);
						} catch (NumberFormatException e) {
							htmlInput.value = "не найден в справочнике";
						}
					}

					StringWriter stringWriter = new StringWriter();
					marshaller.marshal(htmlInput, stringWriter);
					String dstHtmlTag = stringWriter.toString();
					//logger.debug("dst = " + dstHtmlTag);
					sb.append(dstHtmlTag);
				}
			} else break;
		}
		sb.append(html.substring(pos));
		html = sb.toString();
	}

	private String getRefCaptionByRecId(String ref, String refCaption, Long recId) {
		if (!liteRefItems.containsKey(ref)) {
			liteRefItems.put(ref, new HashMap<Long, LiteRefItem>());
		}
		if (!liteRefItems.get(ref).containsKey(recId)) {
			liteRefItems.get(ref).put(recId, new LiteRefItem(ref, recId));
		}
		LiteRefItem refItem = liteRefItems.get(ref).get(recId);
		if (!refItem.captionFields.containsKey(refCaption)) {
			String value = reference.getRefItemNameByRecId(ref, refCaption, recId, reportDate);
			refItem.captionFields.put(refCaption, value);
		}
		return liteRefItems.get(ref).get(recId).captionFields.get(refCaption);
	}

	private void processRefs() throws  JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTextarea.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

		int pos, startPos, endPos, finishPos;
		Map<String, List<InputSelectRecord>> inputSelectData = new HashMap<String, List<InputSelectRecord>>();
        Map<String, String> viewModelRefMap = new HashMap<String, String>();

		String openTag = "<textarea";
		String closeTag = "</textarea>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();
		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				endPos = html.indexOf(closeTag, startPos);
				if (endPos >= 0) {
					pos = endPos + closeTag.length();
				} else break;
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlTextarea htmlTextarea = (HtmlTextarea) unmarshaller.unmarshal(is);

					if (htmlTextarea.ref == null) {
						continue;
					}

					// Process
					Map<String, List<InputSelectRecord>> refs;
					if (htmlTextarea.multiValue != null && htmlTextarea.multiValue.toLowerCase().equals("multivalue"))
						refs = multiRefs;
					else
						refs = singleRefs;

					if (htmlTextarea.viewModel != null && !htmlTextarea.viewModel.isEmpty()) {
						if (!inputSelectViewModels.containsKey(htmlTextarea.viewModel)) {
							logger.error(MessageFormat.format("View model {0} not specified", htmlTextarea.viewModel));
							throw new EJBException(MessageFormat.format("Невозможно открыть отчет, так как модель {0} компонента InputSelect не описан", htmlTextarea.viewModel));
						}
						List<InputSelectRecord> records = reference.getReferenceItemsByNameViewModel(htmlTextarea.ref, reportDate, htmlTextarea.refCaption, inputSelectViewModels.get(htmlTextarea.viewModel));
						if (!inputSelectData.containsKey(htmlTextarea.viewModel)) {
                            viewModelRefMap.put(htmlTextarea.viewModel, htmlTextarea.ref);
							inputSelectData.put(htmlTextarea.viewModel, records);
						}
					} else {
						InputSelectViewModel defaultViewModel = getDefaultViewModel();
						List<InputSelectRecord> records = reference.getReferenceItemsByNameViewModel(htmlTextarea.ref, reportDate, htmlTextarea.refCaption, defaultViewModel);
						inputSelectData.put(htmlTextarea.ref, records);
						viewModelRefMap.put(htmlTextarea.ref, htmlTextarea.ref);
					}
					if (!refs.containsKey(htmlTextarea.ref)) {
                        List<InputSelectRecord> records;
                        if(htmlTextarea.viewModel==null || htmlTextarea.viewModel.isEmpty()){
							records = inputSelectData.get(htmlTextarea.ref);
                        } else {
                            records = inputSelectData.get(htmlTextarea.viewModel);
                        }
						refs.put(htmlTextarea.ref, records);


						/*Reference reference = new Reference();
						reference.setRecId(-1L);
						reference.setCode("-1");
						reference.setNameRu("не найден в справочнике");
						((List<Reference>) refs.get(htmlTextarea.ref)).add(reference);*/

						Map<Long, InputSelectRecord> idx = new HashMap<Long, InputSelectRecord>();
						for (InputSelectRecord ref : refs.get(htmlTextarea.ref)) {
							idx.put(ref.getRecId(), ref);
						}
						refIdx.put(htmlTextarea.ref, idx);
					}
				}
			} else break;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(html);
		sb.append("\n<script>\n");
		for (Map.Entry<String, List<InputSelectRecord>> entry : inputSelectData.entrySet()) {
			String viewModelName = entry.getKey();
            String refName = viewModelRefMap.get(viewModelName);
			String dialogObjectName;
			List<InputSelectRecord> records = inputSelectData.get(viewModelName);
			if (singleRefs.containsKey(refName)) {
				dialogObjectName = "singleDialogObj";
			} else if (multiRefs.containsKey(refName)) {
				dialogObjectName = "multiDialogObj";
			} else {
				continue;
			}
			sb.append("dialogs.push(Object.create(" + dialogObjectName + "));");
			sb.append("dialogs[dialogs.length-1].name = \"").append(viewModelName).append("\";");
			sb.append("dialogs[dialogs.length-1].data =").append(refsToJSONString(viewModelName, records)).append(";");
		}
		sb.append("\n");
		sb.append("var jsons = [\n");
		boolean first = true;
		for (Map.Entry<String, InputSelectViewModel> e : inputSelectViewModels.entrySet()) {
			String json = gson.toJson(e.getValue());
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append("{ name:'" + e.getKey() + "', json:'" + json + "'}");
		}
		sb.append("];\n");
		/*sb.append("viewModels = jsons.map(function (json) {\n" +
				"                try {\n" +
				"                    return JSON.parse(json);\n" +
				"                } catch (e) {\n" +
				"                    console.error(\"Can't parse viewmodel \" + json, e);\n" +
				"                    return {};\n" +
				"                }\n" +
				"            });");*/
		sb.append("\n</script>\n");

		html = sb.toString();
	}

	private InputSelectViewModel getDefaultViewModel(){
		if(defaultViewModel==null) {
			defaultViewModel = new InputSelectViewModel();
			defaultViewModel.setName("_DEFAULT_VIEW_MODEL");
			defaultViewModel.setColumns(new ArrayList<InputSelectColumn>(Arrays.asList(new InputSelectColumn[]{
					new InputSelectColumn("rec_id", "rec_id", true, true, "", ValueType.NUMBER_0),
					new InputSelectColumn("code", "Код", false, false, "", ValueType.STRING),
					new InputSelectColumn("name_ru", "Наименование", false, false, "", ValueType.STRING)
			})));
			defaultViewModel.setSortFields(new ArrayList<SortField>(Arrays.asList(new SortField[]{new SortField("name_ru", false)})));
		}
		return defaultViewModel;
	}

	private void processPickRefs() throws  JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(HtmlInput.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
		int pos, startPos, endPos, finishPos;
		String openTag = "<input";
		String closeTag = "</input>";
		pos = 0;
		finishPos = html.indexOf(FINISH_TAG);
		if (finishPos == -1) finishPos = html.length();

		List<String> pickRefs = new ArrayList<String>();

		while (pos < finishPos) {
			startPos = html.indexOf(openTag, pos);
			if (startPos >= 0) {
				endPos = html.indexOf("/>", startPos); // non-container tag, i.e. can be self-closing
				if (endPos >= 0) {
					pos = endPos + 2;
				} else {
					endPos = html.indexOf(closeTag, startPos);
					if (endPos >= 0) {
						pos = endPos + closeTag.length();
					} else break;
				}
				if (endPos >= 0) {
					String srcHtmlTag = html.substring(startPos, pos);
					//logger.debug("src = " + srcHtmlTag);
					InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
					HtmlInput htmlInput = (HtmlInput) unmarshaller.unmarshal(is);

					if (!htmlInput.type.toLowerCase().equals("button") || htmlInput.ref == null) {
						continue;
					}

					// Process
					if (htmlInput.ref.toLowerCase().equals("bank")) {
						List<RefBankItem> banks = (List<RefBankItem>) reference.getRefAbstractList(RefBankItem.REF_CODE, reportDate);
						pickRefs.add("\"bank\":" + refsToJSONArrayString(banks));
					} else if (htmlInput.ref.toLowerCase().equals("person")) {
						List<RefLegalPersonItem> legalPersonItems = (List<RefLegalPersonItem>) reference.getRefAbstractList(RefLegalPersonItem.REF_CODE, reportDate);
						pickRefs.add("\"person\":" + legalPersonRefsToJSONArrayString(legalPersonItems));

						List<RefCountryItem> refCountryItems = (List<RefCountryItem>)reference.getRefAbstractList(RefCountryItem.REF_CODE, reportDate);
						pickRefs.add("\"country\":" + refsToJSONArrayString(refCountryItems));
					} else if(htmlInput.ref.toLowerCase().equals("legalperson")){
						List<RefLegalPersonItem> legalPersonItems = (List<RefLegalPersonItem>) reference.getRefAbstractList(RefLegalPersonItem.REF_CODE, reportDate);
						pickRefs.add("\"legalperson\":" + legalPersonRefsToJSONArrayString(legalPersonItems));
					} else if(htmlInput.ref.toLowerCase().equals("pick_bank")){
						List<RefBankItem> banks = (List<RefBankItem>)reference.getRefAbstractList(RefBankItem.REF_CODE, reportDate);
						pickRefs.add("\"pick_bank\":" + refsToJSONArrayString(banks));
					} /*else if(htmlInput.ref.toLowerCase().equals("pick_legalperson")){
						List<RefLegalPersonItem> legalPersonItems = (List<RefLegalPersonItem>) reference.getRefAbstractList(RefLegalPersonItem.REF_CODE, reportDate);
						pickRefs.add("\"pick_legalperson\":" + legalPersonRefsToJSONArrayString(legalPersonItems));
					}*/ else if (htmlInput.ref.toLowerCase().equals("pick_person2")) {
						List<RefLegalPersonItem> legalPersonItems = (List<RefLegalPersonItem>) reference.getRefAbstractList(RefLegalPersonItem.REF_CODE, reportDate);
						pickRefs.add("\"pick_person2\":" + legalPersonRefsToJSONArrayString(legalPersonItems));

						List<RefCountryItem> refCountryItems = (List<RefCountryItem>)reference.getRefAbstractList(RefCountryItem.REF_CODE, reportDate);
						pickRefs.add("\"country\":" + refsToJSONArrayString(refCountryItems));
					}


				}
			} else break;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(html);
		sb.append("\n");
		//sb.append("\n<script>\n<![CDATA[\n");
		sb.append("\n<script>\n");
		sb.append(" var reference_data = {");
		for (int i = 0; i < pickRefs.size(); i++) {
			if (i > 0)
				sb.append(",");
			sb.append(pickRefs.get(i));
		}
		sb.append("};");
		sb.append("normalizeData(reference_data);");
		//sb.append("\n]]>\n</script>");
		sb.append("\n</script>");

		html = sb.toString();
	}

	private boolean isInputSelectTextArea(HtmlTextarea htmlTextarea){
		boolean isInputSelect = false;
		if (htmlTextarea.clazz != null) {
			for (String clazz : htmlTextarea.clazz.split(" ")) {
				if (clazz.trim().equals("inputSelect")) {
					isInputSelect = true;
					break;
				}
			}
		}
		return isInputSelect;
	}

	private boolean isReceiverCaption(HtmlTag htmlTag){
		if (htmlTag.clazz != null) {
			for (String clazz : htmlTag.clazz.split(" ")) {
				if (clazz.trim().equals("receiverInput")) {
					return true;
				}
			}
		}
		return false;
	}

	private String refsToJSONString(String viewModelName, List<InputSelectRecord> refs) {
		InputSelectViewModel vm = inputSelectViewModels.get(viewModelName);
		if (vm == null) {
			vm = getDefaultViewModel();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		List<InputSelectColumn> columns = new ArrayList<InputSelectColumn>();
		boolean hasRecIdColumn = false;
		for (InputSelectColumn column : vm.getColumns()) {
			columns.add(column);
			if (!hasRecIdColumn && column.getName().equalsIgnoreCase("rec_id")) {
				hasRecIdColumn = true;
			}
		}
		if (!hasRecIdColumn) {
			columns.add(new InputSelectColumn("rec_id", "rec_id", true, true, "", ValueType.NUMBER_0));
		}
		sb.append("columns:").append(gson.toJson(columns)).append(",");
		sb.append("items:").append(refsToJSONArrayString(refs, columns)).append("}");
		return sb.toString();
	}

	private String refsToJSONArrayString(List<InputSelectRecord> refs, List<InputSelectColumn> columns){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < refs.size(); i++) {
			InputSelectRecord reference = refs.get(i);
			if (i > 0)
				sb.append(",");
			StringBuilder rec = new StringBuilder();
			rec.append("{ ");
			for (int colIndex = 0; colIndex < columns.size(); colIndex++) {
				InputSelectColumn column = columns.get(colIndex);
				String value;
				try {
					value = DataType.variantToString(reference.get(column.getName()), column.getValueType());
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage());
				}
				if (!column.getName().equalsIgnoreCase("rec_id")) {
					value = "\"" + escape(value) + "\"";
				}
				if (colIndex > 0) {
					rec.append(",");
				}
				rec.append("\"" + column.getName() + "\":").append(value);
			}
			rec.append("}");
			sb.append("\""+Convert.encodeURIComponent(rec.toString())+"\"");
		}
		sb.append("]");
		return sb.toString();
	}

	private String legalPersonRefsToJSONArrayString(List<RefLegalPersonItem> refs){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < refs.size(); i++) {
			RefLegalPersonItem reference = refs.get(i);
			if (i > 0)
				sb.append(",");
			StringBuilder rec = new StringBuilder();
			rec.append("{ ")
					.append("\"rec_id\"").append(":").append(reference.getRecId().toString()).append(",")
					.append("\"idn\"").append(":").append("\"").append(reference.getIdn() == null ? "" : reference.getIdn()).append("\"").append(",")
					.append("\"name_ru\"").append(":").append("\"").append(reference.getNameRu() == null ? "" : escape(reference.getNameRu())).append("\"").append(",")
					.append("\"ref_org_type_rec_id\"").append(":").append((reference.getRefOrgTypeRecId() == null || reference.getRefOrgTypeRecId() == 0) ? ("\""+""+"\"") : reference.getRefOrgTypeRecId().longValue()).append(",")
					.append("\"country_id\"").append(":").append((reference.getRefCountryRecId() == null || reference.getRefCountryRecId() == 0) ? ("\""+""+"\"") : reference.getRefCountryRecId().longValue()).append(",")
					.append("\"country_name\"").append(":").append("\"").append((reference.getRefCountryRecId() == null || reference.getRefCountryRecId() == 0) ? "" : escape(reference.getCountryName())).append("\"")
					.append(" }");
			sb.append("\""+Convert.encodeURIComponent(rec.toString())+"\"");
		}
		sb.append("]");
		return sb.toString();
	}

	private String refsToJSONArrayString(List<? extends AbstractReference> refs){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < refs.size(); i++) {
			AbstractReference reference = refs.get(i);
			String refRecIdValue = "";
			String refCodeValue = "";
			String refCaptionValue = "";
			refRecIdValue = reference.getRecId().toString();
			refCodeValue = "\"" + escape(reference.getCode() != null ? reference.getCode() : "") + "\"";
			refCaptionValue = "\"" + escape(reference.getNameRu() != null ? reference.getNameRu() : "") + "\"";
			if (i > 0)
				sb.append(",");
			StringBuilder rec = new StringBuilder();
			rec.append("{ ")
					.append("\"rec_id\"").append(":").append(refRecIdValue).append(",")
					.append("\"code\"").append(":").append(refCodeValue).append(",")
					.append("\"name_ru\"").append(":").append(refCaptionValue)
					.append(" }");
			sb.append("\""+Convert.encodeURIComponent(rec.toString())+"\"");
		}
		sb.append("]");
		return sb.toString();
	}

	private String escape(String s) {
		if (s != null) {
			s = s.replace("\\", "\\\\");
			s = s.replace("\t"," ").replace("\r\n"," ").replace("\n"," ");
			s = s.replaceAll("\"", "\\\\\"");
		}
		return s;
	}

	public String getHtml() {
		return html;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	class LiteRefItem{
		String refName;
		Long recId;
		Map<String, String> captionFields;

		public LiteRefItem(String refName, Long recId) {
			this.refName = refName;
			this.recId = recId;
			captionFields = new HashMap<String, String>();
		}
	}

	public static void main(String[] args){
		Gson gson = new Gson();
		List<InputSelectColumn> columns = new ArrayList<InputSelectColumn>();
		for (int i = 1; i < 5; i++) {
			InputSelectColumn column = new InputSelectColumn();
			column.setName("col" + Integer.toString(i));
			columns.add(column);
		}
		String json = gson.toJson(columns);
		System.out.println(json);
	}
}
