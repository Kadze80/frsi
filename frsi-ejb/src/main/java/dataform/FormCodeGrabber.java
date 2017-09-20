package dataform;

import ejb.Persistence;
import entities.RefCrosscheckForm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Nuriddin.Baideuov on 16.06.2015.
 */
public class FormCodeGrabber implements IKeyHandler {

    private Set<String> formCodes;

    public FormCodeGrabber() {
        formCodes = new HashSet<String>();
    }

    @Override
    public String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError {
        if (key.indexOf(";") < 0)
            throw new FormulaSyntaxError();

        String formCode_ = key.substring(0, key.indexOf(";"));
        if (formCode_.length() == 0)
            throw new FormulaSyntaxError();


        formCode_ = formCode_.toLowerCase();

        if (!formCodes.contains(formCode_)) {
            formCodes.add(formCode_);
        }

        return "0";
    }

    public Set<String> getFormCodes() {
        return formCodes;
    }

    public void setFormCodes(Set<String> formCodes) {
        this.formCodes = formCodes;
    }
}
