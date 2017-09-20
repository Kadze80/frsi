package form.calcfield2;

import com.google.gson.Gson;
import form.calcfield.IDataProvider;
import util.Convert;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nuriddin on 10/6/16.
 */
public class CalcField2 {
    private ScriptEngine scriptEngine;
    private Writer writer;

    public CalcField2(ScriptEngine scriptEngine) throws Exception {
        this.scriptEngine = scriptEngine;
        init();
    }

    public void init() throws Exception {
        Reader common = getFile("js/common.js");
        Reader form = getFile("js/form2.js");
        scriptEngine.eval(common);
        scriptEngine.eval(form);
        common.close();
        form.close();
    }

    private static Reader getFile(String fileName) throws Exception {
        URL url = CalcField2.class.getClassLoader().getResource(fileName);
        if (url == null)
            throw new Exception(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        File scriptFile = new File(url.getFile());
        if (!scriptFile.exists()) {
            throw new Exception(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        }
        return Files.newBufferedReader(scriptFile.toPath(), StandardCharsets.UTF_8);
    }

    public void updateCalculatedFields(String script) throws ScriptException {
        scriptEngine.eval(script);
        scriptEngine.eval("updateCalculatedFields();");
    }

    public void setProvider(IDataProvider2 dataProvider) {
        scriptEngine.put("p", dataProvider);
    }

    public static void main(String[] args) throws Exception {
        /*ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        CalcField2 cf = new CalcField2();
        sc.put("cf", cf);
        sc.eval("var cb = function(){ return cf.getInt()+5;};");
        sc.eval("var callback = new Packages.form.calcfield2.DoubleCallback(cb);");
        sc.eval("cf.call(callback);");

        sc.eval("var cb2 = function(){ return cf.getInt() + 3.1;};");
        sc.eval("var callback2 = new Packages.form.calcfield2.StringCallback(cb2);");
        sc.eval("cf.call(callback2);");

        sc.eval("var cb3 = function(){ return 99;};");
        sc.eval("var callback3 = new Packages.form.calcfield2.ObjectCallback(cb3);");
        sc.eval("cf.call(callback3);");*/
//        sc.eval("var v = cf.getInt(); v = v+3; cf.print(v);");
//        testProvider();
//        testRange();
//        testRange2();
//        testEachRow();
        testCount();
    }

    private static void testProvider() throws ScriptException {
        Map<String, String> kvMap = new HashMap<String, String>();
        double res = 0;
        for (int i = 1; i <= 5; i++) {
            kvMap.put("f1_array*v1:n:$D.1." + String.valueOf(i), String.valueOf(i * 0.5));
            res += i;
        }

        IDataProvider2 p = new DataProvider2(kvMap);
        ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        sc.put("p", p);
        sc.eval("var impl = function(){ return p.getNumber('f1_array*v1:n:$DR');};");
        sc.eval("var cb = new Packages.form.calcfield2.DoubleCallback(impl);");
        Object sum = sc.eval("p.sumDynRow(cb, '$D.1.n', 'f1_array');");
        System.out.println(res);
        System.out.println(sum);
    }

    private static void testRange() throws ScriptException {
        Map<String, String> kvMap = new HashMap<String, String>();
        double res = 0;
        for (int i = 1; i <= 5; i++) {
            kvMap.put("f1_array*v1:n:$D.1." + String.valueOf(i), String.valueOf(i));
            res += i;
        }

        IDataProvider2 p = new DataProvider2(kvMap);
        ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        sc.put("p", p);
        sc.eval("var impl = function(){ return p.getNumber('f1_array*v1:n:$DR');};");
        sc.eval("var cb = new Packages.form.calcfield2.DoubleCallback(impl);");
        Object sum = sc.eval("p.sumRange(cb, 'ROW', '$D.1.1', '$D.1.4', 'f1_array');");
        System.out.println(sum);
    }

    private static void testRange2() throws ScriptException {
        Map<String, String> kvMap = new HashMap<String, String>();
        double res = 0;
        List<String> fields = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            String v = "v" + String.valueOf(i);
            fields.add(v);
            kvMap.put("f1_array*" + v + ":n:1", String.valueOf(i));
            res += i;
        }
        Map<String, List<String>> fm = new HashMap<String, List<String>>();
        fm.put("f1_array", fields);

        IDataProvider2 p = new DataProvider2(kvMap);
        p.setFields(fm);
        ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        sc.put("p", p);
        sc.eval("var impl = function(){ return p.getNumber('f1_array*$DC:n:1');};");
        sc.eval("var cb = new Packages.form.calcfield2.DoubleCallback(impl);");
        Object sum = sc.eval("p.sumRange(cb, 'FIELD', 'v1', 'v4', 'f1_array');");
        System.out.println(sum);
    }

    private static void testEachRow() throws ScriptException {
        Map<String, String> kvMap = new HashMap<String, String>();
        double res = 0;
        for (int i = 1; i <= 5; i++) {
            kvMap.put("f1_array*v1:n:$D.1." + String.valueOf(i), String.valueOf(i));
            res += i;
        }

        IDataProvider2 p = new DataProvider2(kvMap);
        ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        sc.put("p", p);
        sc.eval("var impl = function(){ return p.getNumber('f1_array*v1:n:$DR')*10+0.1;};");
        sc.eval("var cb = new Packages.form.calcfield2.ObjectCallback(impl);");
        sc.eval("p.eachRow('f1_array*v2:n:$DR', cb, '$D.1.n', 'f1_array', 'n2');");


        Gson gson = new Gson();
        System.out.println(gson.toJson(kvMap));
    }

    private static void testCount() throws Exception{
        Map<String, String> kvMap = new HashMap<String, String>();
        for (int i = 1; i <= 5; i++) {
            for (int k = 1; k <= 3; k++) {
                kvMap.put("f1_array*v" + k + ":n:$D.1." + String.valueOf(i), String.valueOf(i));
            }
        }
        IDataProvider2 p = new DataProvider2(kvMap);
        ScriptEngine sc = new ScriptEngineManager().getEngineByName("JavaScript");
        sc.put("p", p);
        Reader common = getFile("js/common.js");
        Reader form = getFile("js/form2.js");
        sc.eval(common);
        sc.eval(form);
        common.close();
        form.close();
        Object count =sc.eval("count('$D.1.n', 'f1_array');");
        System.out.println(count);

    }

    public Object getObject() {
        return 85;
    }

    public int getInt() {
        return 85;
    }

    public void print(Object v) {
        System.out.println(v);
    }

    public void call(DoubleCallback cb) {
        double d = cb.call();
        System.out.println(d);
    }

    public void call(StringCallback cb) throws Exception {
        String d = cb.call();
        System.out.println(Convert.getNumWithMaskFromStr(d, "n0"));
    }

    public void call(ObjectCallback cb) {
        Object d = cb.call();
        System.out.println(d);
    }
}
