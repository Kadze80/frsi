package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuriddin on 1/20/17.
 */
public class SqlParamParser extends TextParser {
    private StringBuilder res = new StringBuilder();
    private List<Param> params = new ArrayList<Param>();
    private int paramIndex;

    public class Param {
        private int index;
        private String name;

        public Param(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Param{" +
                    "index=" + index +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public SqlParamParser(String sqltext) {
        loadFrom(sqltext);
    }

    public List<Param> getParams() {
        return params;
    }

    public String getResult() {
        return res.toString();
    }

    protected void onParse() throws Exception {
        paramIndex = 0;
        while (true) {
            char c = next();
            if (c == EOF) {
                break;
            }
            if (c == '\\') {
                char c2 = next();
                if (c2 == ':') {
                    res.append(c2);
                } else {
                    res.append(c);
                    push(c2);
                }
            } else if (c == ':') {
                char c2 = next();
                if (c2 == '=') {
                    res.append(c);
                    push(c2);
                } else {
                    String s;
                    if (c2 == '{') {
                        s = grabUntil('}');
                    } else {
                        push(c2);
                        s = grabIdn();
                    }
                    params.add(new Param(++paramIndex, s));
                    res.append(":" + paramIndex);
                }
            } else {
                res.append(c);
            }
        }
    }

    private String grabIdn() throws Exception {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                sb.append(c);
            } else {
                push(c);
                break;
            }
        }
        return sb.toString();
    }

    private String grabUntil(char terminate) throws Exception {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c != terminate && c != EOF) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String sql = "begin select :p1 into :r from dual; end;";
        SqlParamParser parser = new SqlParamParser(sql);
        List<Param> params = parser.getParams();
        System.out.println(params);
        System.out.println(parser.getResult());
    }
}
