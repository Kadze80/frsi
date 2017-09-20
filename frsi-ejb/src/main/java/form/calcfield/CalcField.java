package form.calcfield;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 15.09.2015.
 */
public class CalcField {

    private ScriptEngine scriptEngine;
    private Writer writer;

    public CalcField(ScriptEngine scriptEngine) throws Exception {
        this.scriptEngine = scriptEngine;
        init();
    }

    public void init() throws Exception {
        Reader common = getFile("js/common.js");
        Reader form = getFile("js/form.js");
        scriptEngine.eval(common);
        scriptEngine.eval(form);
        common.close();
        form.close();
    }

    private Reader getFile(String fileName) throws Exception {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null)
            throw new Exception(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        File scriptFile = new File(url.getFile());
        if (!scriptFile.exists()) {
            throw new Exception(MessageFormat.format("Js-файл {0} полей не найден", fileName));
        }
        return Files.newBufferedReader(scriptFile.toPath(), StandardCharsets.UTF_8);
    }

    public void updateCalculatedFields(String script) throws ScriptException {
        /*try {
        File wFile = new File("d:\\output.txt");
        if(!wFile.exists()) wFile.createNewFile();
        writer = Files.newBufferedWriter(wFile.toPath(), StandardCharsets.UTF_8);
        scriptEngine.getContext().setWriter(writer);

        scriptEngine.eval(script);
        scriptEngine.put("document", dataProvider);
        scriptEngine.eval("updateCalculatedFields();");
        }catch (IOException e){

        }finally {
            try {
                writer.close();
            }catch (IOException e){}

        }*/
        scriptEngine.eval(script);
        scriptEngine.eval("updateCalculatedFields();");
    }

    public void setProvider(IDataProvider dataProvider) {
        scriptEngine.put("document", dataProvider);
    }

}
