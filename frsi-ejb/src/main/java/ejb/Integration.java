package ejb;

import entities.NsiListItem;

import java.util.Date;
import java.util.List;

public interface Integration {

    void init();

    // Front-end Server
    Date getNewDateFromBackEndServer();

    int getPageReloadDuration();
    int getAutosaveDuration();

    // KISC
    String getSignatureInfo(String caUrl, String signature, String respondentBin, String respondentIdn, String languageCode);

}
