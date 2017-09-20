package mb;

import entities.DataColumn;
import entities.DataRecord;
import entities.DataSet;
import entities.QueryType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * Created by nuriddin on 10/21/16.
 */
@ManagedBean
@RequestScoped
public class QueryBean {
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private String queryString;
    private String queryType = QueryType.SELECT.name();
    private DataSet dataSet;
    private String queryResult;

    public void execute() {
        dataSet = sessionBean.getPersistence().execQuery(queryString, QueryType.valueOf(queryType));
        formatResult();
    }

    private void formatResult() {
        QueryType qt = QueryType.valueOf(queryType);
        if (qt == QueryType.INSERT_OR_UPDATE) {
            queryResult = "";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<table class=\"dataTable\">")
                    .append("<thead>")
                    .append("<tr>");
            for (DataColumn col : dataSet.getColumns()) {
                sb.append("<th>").append(col.getName()).append("</th>");
            }
            sb.append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");
            for (DataRecord rec : dataSet.getRecords()) {
                sb.append("<tr>");
                for (DataColumn col : dataSet.getColumns()) {
                    sb.append("<td>")
                            .append(rec.get(col.getIndex()))
                            .append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</tbody>").append("</table>");
            queryResult = sb.toString();
        }
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQueryResult() {
        return queryResult;
    }
}
