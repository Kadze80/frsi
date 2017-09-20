package util;

import entities.Param;
import entities.ValueType;
import entities.Variant;

import javax.ejb.EJBException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlBlockExecutor {

    public static Variant execute(Connection connection, String alg, Param[] params, ValueType resultValueType) throws SQLException {
        CallableStatement stmt = null;
        Variant resultValue = null;

        SqlParamParser parser = new SqlParamParser(alg);
        List<SqlParamParser.Param> sqlParams = parser.getParams();
        String tAlg = parser.getResult().replace("'","''");

        StringBuilder sql = new StringBuilder();
        sql.append("BEGIN EXECUTE IMMEDIATE")
                .append(" '").append(tAlg).append("'");
        if (sqlParams.size() > 0)
            sql.append(" USING");

        boolean firstParam = true;
        for (SqlParamParser.Param sqlParam : sqlParams) {

            if (firstParam)
                firstParam = false;
            else
                sql.append(",");

            if (sqlParam.getName().equalsIgnoreCase("result"))
                sql.append(" OUT");
            else
                sql.append(" IN");
            sql.append(" ?");
        }
        sql.append("; END;");

        Map<String, Param> paramMap = new HashMap<String, Param>();
        for (Param p : params) {
            paramMap.put(p.getName().toLowerCase(), p);
        }

        try {
            stmt = connection.prepareCall(sql.toString());

            int resultType;
            switch (resultValueType) {
                case STRING:
                    resultType = Types.VARCHAR;
                    break;
                case BOOLEAN:
                    resultType = Types.BOOLEAN;
                    break;
                case DATE:
                    resultType = Types.DATE;
                    break;
                case NUMBER_0:
                case NUMBER_1:
                case NUMBER_2:
                case NUMBER_3:
                case NUMBER_4:
                case NUMBER_5:
                case NUMBER_6:
                case NUMBER_7:
                case NUMBER_8:
                    resultType = Types.NUMERIC;
                    break;
                default:
                    throw new EJBException("Result type not set");
            }
            int index = 1;
            int resultIndex = 0;
            for (SqlParamParser.Param sqlParam : sqlParams) {
                if (sqlParam.getName().equalsIgnoreCase("result")) {
                    stmt.registerOutParameter(index, resultType);
                    resultIndex = index;
                } else {
                    Param param = paramMap.get(sqlParam.getName().toLowerCase());
                    if (param == null) {
                        throw new EJBException(MessageFormat.format("Параметр {0} найден в алгоритме, но значение не передано", sqlParam.getName()));
                    }
                    Variant value = param.getValue();
                    switch (value.getValueType()) {
                        case STRING:
                            stmt.setString(index, value.getStrValue());
                            break;
                        case BOOLEAN:
                            stmt.setBoolean(index, value.getBoolValue());
                            break;
                        case DATE:
                            stmt.setDate(index, value.getDateValue() != null ? new java.sql.Date(value.getDateValue().getTime()) : null);
                            break;
                        case NUMBER_0:
                            stmt.setLong(index, value.getLngValue());
                            break;
                        case NUMBER_1:
                        case NUMBER_2:
                        case NUMBER_3:
                        case NUMBER_4:
                        case NUMBER_5:
                        case NUMBER_6:
                        case NUMBER_7:
                        case NUMBER_8:
                            stmt.setDouble(index, value.getDblValue());
                            break;
                    }
                }

                index++;
            }
            stmt.execute();

            resultValue = new Variant();
            resultValue.setValueType(resultValueType);
            switch (resultValue.getValueType()) {
                case STRING:
                    resultValue.setStrValue(stmt.getString(resultIndex));
                    break;
                case BOOLEAN:
                    resultValue.setBoolValue(stmt.getBoolean(resultIndex));
                    break;
                case DATE:
                    resultValue.setDateValue(stmt.getDate(resultIndex));
                    break;
                case NUMBER_0:
                    resultValue.setLngValue(stmt.getLong(resultIndex));
                    break;
                case NUMBER_1:
                case NUMBER_2:
                case NUMBER_3:
                case NUMBER_4:
                case NUMBER_5:
                case NUMBER_6:
                case NUMBER_7:
                case NUMBER_8:
                    resultValue.setDblValue(stmt.getDouble(resultIndex));
                    break;
            }
            if (stmt.wasNull()) {
                resultValue = null;
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
            }
        }

        return resultValue;
    }
}
