package ejb;

import javax.ejb.Local;

/**
 * Created by nuriddin on 12/23/16.
 */
@Local
public interface PdfConverterLocal {
    void init();
//    void setTimerService();
    String convertWord(String fileFullPath,String pdfFileFullPath) throws Exception;
    String convertExcel(String fileFullPath,String pdfFileFullPath) throws Exception;
    String unionExcel(String dirPath) throws Exception;
}
