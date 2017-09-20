package ejb;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import java.text.MessageFormat;
import java.util.Date;
import soap.excel.ExcelService;
import soap.unionexcel.UnionExcelService;
import soap.word.WordService;

/**
 * Created by nuriddin on 12/23/16.
 */
@Stateless
public class PdfConverterBean implements PdfConverterLocal {
    private static final Logger logger = Logger.getLogger("fileLogger");

    private static final Logger LOG = Logger.getLogger(PdfConverterBean.class);
    private static String TEMP_DIR = "d:\\temp";

    /*@Resource
    private TimerService timerService;*/
    private ExcelService excelSoapService = null;
    private WordService wordSoapService = null;
    private UnionExcelService unionExcelService = null;


    @PostConstruct
    @Override
    public void init() {
        Date date1 = new Date();

        String downloadTempDir = System.getProperty("frsi.tempdir");
        if (downloadTempDir != null) TEMP_DIR = downloadTempDir;

        logger.info("Start setTimerService");
//        setTimerService();
        Date date2 = new Date();
        long duration = date2.getTime() - date1.getTime();
        logger.debug(MessageFormat.format("end setTimerService t: {0}, d: {1} ms", date1, duration));
        logger.info("Start initSoapService");
        initExcelSoapService();
        initWordSoapService();
        initUnionExcelSoapService();
        Date date3 = new Date();
        duration = date3.getTime() - date2.getTime();
        logger.debug(MessageFormat.format("end initSoapService t: {0}, d: {1} ms", date2, duration));
    }

    @PreDestroy
    public void preDestroy() {
        excelSoapService = null;
        wordSoapService = null;
        unionExcelService = null;
    }

    /*@Override
    public void setTimerService() {
        String minute = "*//*10";

        String timer = System.getProperty("frsi.pdf.converter.timer"); // Формат таймера : hh:mm:ss
        // logger.info("Timer:" + timer);

        if (timer != null) {
            minute = timer;
        }
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("*").minute(minute);
//        schedule.hour("*").minute("*//*1");//for testing - sets for every minute
        timerService.createCalendarTimer(schedule);
    }*/

    @Override
    public String convertWord(String fileFullPath, String pdfFileFullPath) throws Exception {
        if (wordSoapService == null) {
            initWordSoapService();
        }
        if (wordSoapService != null) {
            return wordSoapService.getWordServiceSoap().convert(fileFullPath, pdfFileFullPath);
        } else {
            throw new Exception("Word soap service is not initialized");
        }
    }

    @Override
    public String convertExcel(String fileFullPath, String pdfFileFullPath) throws Exception {
        if (excelSoapService == null) {
            initExcelSoapService();
        }
        if (excelSoapService != null) {
            return excelSoapService.getExcelServiceSoap().convert(fileFullPath, pdfFileFullPath);
        } else {
            throw new Exception("Excel soap service is not initialized");
        }
    }

    @Override
    public String unionExcel(String dirPath) throws Exception {
        if (unionExcelService == null) {
            initUnionExcelSoapService();
        }
        if (unionExcelService != null) {
            return unionExcelService.getUnionExcelServiceSoap().union(dirPath);
        } else {
            throw new Exception("Excel soap service for union files is not initialized");
        }
    }

    /*@Timeout
    public void timeOutExecute() {
        // wake up Excel Service
        try {
            String fileFullPath = TEMP_DIR + "\\__test__.xlsx";
            String pdfFileFullPath = TEMP_DIR + "\\__test__xlsx.pdf";
            String result = convertExcel(fileFullPath, pdfFileFullPath);
            LOG.info(MessageFormat.format("Excel Pdf converter test successful: {0}", result));
        } catch (Exception e) {
            LOG.warn(MessageFormat.format("Excel Pdf converter test error: {0}", e.getMessage()));
        }

        // wake up Word Service
        try {
            String fileFullPath = TEMP_DIR + "\\__test__.docx";
            String pdfFileFullPath = TEMP_DIR + "\\__test__docx.pdf";
            String result = convertWord(fileFullPath, pdfFileFullPath);
            LOG.info(MessageFormat.format("Word Pdf converter test successful: {0}", result));
        } catch (Exception e) {
            LOG.warn(MessageFormat.format("Word Pdf converter test error: {0}", e.getMessage()));
        }
    }*/

    private void initWordSoapService() {
        String testMode = System.getProperty("frsi.testmode");
        if (testMode == null || testMode.toLowerCase().equals("false")) {
            try {
                wordSoapService = new WordService();
            } catch (Exception e) {
                LOG.debug("Not initialized. Could not initialize Word Soap Service");
            }
        }
    }

    private void initExcelSoapService() {
        String testMode = System.getProperty("frsi.testmode");
        if (testMode == null || testMode.toLowerCase().equals("false")) {
            try {
                excelSoapService = new ExcelService();
            } catch (Exception e) {
                LOG.debug("Not initialized. Could not initialize Excel Soap Service");
            }
        }
    }

    private void initUnionExcelSoapService() {
        String testMode = System.getProperty("frsi.testmode");
        if (testMode == null || testMode.toLowerCase().equals("false")) {
            try {
                unionExcelService = new UnionExcelService();
            } catch (Exception e) {
                LOG.debug("Not initialized. Could not initialize Union Excel Soap Service");
            }
        }
    }
}
