package parser;

import entities.SubjectType_Form;

import java.util.*;

/**
 * Created by nuriddin on 12/23/16.
 */
public abstract class SubjectTypeFormIndex {

    private class SubjectTypeFormKey {
        long stRecId;
        String formCode;

        public SubjectTypeFormKey(long stRecId, String formCode) {
            this.stRecId = stRecId;
            this.formCode = formCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubjectTypeFormKey that = (SubjectTypeFormKey) o;

            if (stRecId != that.stRecId) return false;
            return formCode.equals(that.formCode);

        }

        @Override
        public int hashCode() {
            int result = (int) (stRecId ^ (stRecId >>> 32));
            result = 31 * result + formCode.hashCode();
            return result;
        }
    }

    private Map<SubjectTypeFormKey, SubjectType_Form> subjectTypeFormMap = new HashMap<SubjectTypeFormKey, SubjectType_Form>();
    private Set<Long> loadedSubjectTypes = new HashSet<Long>();

    protected abstract List<SubjectType_Form> loadSubjectTypeForms(long stRecId);

    public SubjectType_Form getSubjectTypeForm(long stRecId, String formCode) {
        if (!loadedSubjectTypes.contains(stRecId)) {
            List<SubjectType_Form> forms = loadSubjectTypeForms(stRecId);
            loadedSubjectTypes.add(stRecId);
            for (SubjectType_Form stForm : forms) {
                SubjectTypeFormKey k = new SubjectTypeFormKey(stRecId, stForm.getFormCode());
                subjectTypeFormMap.put(k, stForm);
            }
        }
        return subjectTypeFormMap.get(new SubjectTypeFormKey(stRecId, formCode));
    }
}
