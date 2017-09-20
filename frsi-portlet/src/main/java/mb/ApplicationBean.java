package mb;

import com.google.gson.Gson;
import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import ejb.*;
import entities.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.primefaces.model.StreamedContent;
import util.ConnectionLostException;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.*;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    public static Long FIRST_LEVEL_BANK_REC_ID = 7L;

    private String orbHost = "localhost"; // Set JVM option -Dfrsi.back.orb.host in GlassFish to override this default value.
    private String orbPort = "3700";      // Set JVM option -Dfrsi.back.orb.port in GlassFish to override this default value.
    private String testPort = "80";      // Set JVM option -Dfrsi.back.test.port in GlassFish to override this default value.
    private Properties orbProps;
    private InitialContext orbContext;

	public Gson gson = new Gson();

	@PostConstruct
    public void init() {
        Date dateStart = new Date();

        String propOrbHost = System.getProperty("frsi.back.orb.host");
        if (propOrbHost != null) orbHost = propOrbHost;
        String propOrbPort = System.getProperty("frsi.back.orb.port");
        if (propOrbPort != null) orbPort = propOrbPort;
        // logger.debug("ORB server = " + orbHost + ":" + orbPort);
        String propTestPort = System.getProperty("frsi.back.test.port");
        if (propTestPort != null) testPort = propTestPort;
        // logger.debug("Test server = " + orbHost + ":" + testPort);
        orbProps = new Properties();
        orbProps.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
        orbProps.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
        orbProps.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
        orbProps.setProperty("org.omg.CORBA.ORBInitialHost", orbHost); // optional.  Defaults to localhost.  Only needed if web server is running on a different host than the appserver.
        orbProps.setProperty("org.omg.CORBA.ORBInitialPort", orbPort); // optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
        // logger.debug("ApplicationBean 4");
        InitialContext initialContext = null;
        try {
            orbContext = new InitialContext(orbProps);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
	}

    public String getLiferayFacesBridgeVersion() {
        return com.liferay.faces.bridge.BridgeImpl.class.getPackage().getImplementationVersion();
    }

    public CoreRemote getCoreRemote() {
        CoreRemote result = null;
        try {
            result = (CoreRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/CoreBean!ejb.CoreRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public FormDaoRemote getFormDaoRemote() {
        FormDaoRemote result = null;
        try {
            result = (FormDaoRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/FormDaoImpl!ejb.FormDaoRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public PersistenceRemote getPersistenceEjb() {
        PersistenceRemote result = null;
        try {
            result = (PersistenceRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/PersistenceBean!ejb.PersistenceRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ScheduleRemote getScheduleEjb() {
        ScheduleRemote result = null;
        try {
            result = (ScheduleRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/ScheduleBean!ejb.ScheduleRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public IntegrationRemote getIntegrationEjb() {
        IntegrationRemote result = null;
        try {
            result = (IntegrationRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/IntegrationBean!ejb.IntegrationRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public PerformControlRemote getPerformControlEjb() {
        PerformControlRemote result = null;
        try {
            result = (PerformControlRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/PerformControlBean!ejb.PerformControlRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public OutputReportsRemote getOutputReportsEjb() {
        OutputReportsRemote result = null;
        try {
            result = (OutputReportsRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/OutputReportsBean!ejb.OutputReportsRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public ReferenceRemote getReferenceEjb() {
        ReferenceRemote result = null;
        try {
            result = (ReferenceRemote) orbContext.lookup("java:global/frsi-ear/frsi-ejb/ReferenceBean!ejb.ReferenceRemote");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getTestMessage() {
        return "Test message from ApplicationBean";
    }

    public String getLiferayFacesResponseNamespace() {
        return com.liferay.faces.bridge.context.BridgeContext.getCurrentInstance().getResponseNamespace();
    }

	public User getUser() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ThemeDisplay themeDisplay = (ThemeDisplay)facesContext.getExternalContext().getRequestMap().get(WebKeys.THEME_DISPLAY);
		return themeDisplay.getUser();
	}

	public Locale getLocale() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ThemeDisplay themeDisplay = (ThemeDisplay)facesContext.getExternalContext().getRequestMap().get(WebKeys.THEME_DISPLAY);
		return themeDisplay.getLocale();
	}

	public ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle("util.UTF8ResourceBundle", getLocale());
	}

    public void redirectToErrorPage(Throwable e) {
        String messageSummary = e.getMessage();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String messageDetails = sw.toString();

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageSummary, messageDetails);

        if (e instanceof ConnectionLostException) {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
            navigationHandler.handleNavigation(facesContext, null, "/views/conn_error?faces-redirect=true");
            facesContext.renderResponse();

        } else {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
            sessionMap.put("errorMessage", message);

            NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
            navigationHandler.handleNavigation(facesContext, null, "/views/error?faces-redirect=true");
            facesContext.renderResponse();
        }
    }

	public static ServletRequest getPortalRequest() {
		Map<String,Object> requests = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
		for (String requestName : requests.keySet()) {
			if (requests.get(requestName) instanceof HttpServletRequestWrapper)
				return ((HttpServletRequestWrapper) requests.get(requestName)).getRequest();
		}
		return null;
	}

	// Themedisplay is only set during the renderphase.
	// To use it during the execute phase in request scoped beans (such as clicking on btn) use the following method
	public ThemeDisplay getThemeDisplay() {
		return (ThemeDisplay) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(WebKeys.THEME_DISPLAY);
	}

    public void redirectPortalToPage(String page) { // example: /web/guest/home
        LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
        try {
            liferayFacesContext.getExternalContext().redirect(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void redirectPortletToView(String view) { // example: /views/download/view.xhtml
		ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
		PortletRequest portletRequest = (PortletRequest) extContext.getRequest();
		ThemeDisplay themeDisplay = (ThemeDisplay)extContext.getRequestMap().get(WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletURLFactoryUtil.create(portletRequest, PortalUtil.getPortletId(portletRequest),
				themeDisplay.getLayout().getPlid(), PortletRequest.ACTION_PHASE);

		portletURL.setParameter("_facesViewIdRender", view);
		try {
			portletURL.setWindowState(WindowState.NORMAL);
			portletURL.setPortletMode(PortletMode.VIEW);
			extContext.redirect(portletURL.toString());
		}
		catch (WindowStateException e) { e.printStackTrace(); }
		catch (PortletModeException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
    }

	public String getOutcomeUrl(String view) {
		ExternalContext extContext = FacesContext.getCurrentInstance().getExternalContext();
		PortletRequest portletRequest = (PortletRequest) extContext.getRequest();
		ThemeDisplay themeDisplay = (ThemeDisplay)extContext.getRequestMap().get(WebKeys.THEME_DISPLAY);

		PortletURL portletURL = PortletURLFactoryUtil.create(portletRequest, PortalUtil.getPortletId(portletRequest),
				themeDisplay.getLayout().getPlid(), PortletRequest.ACTION_PHASE);

		portletURL.setParameter("_facesViewIdRender", view);
		try {
			portletURL.setWindowState(WindowState.NORMAL);
			portletURL.setPortletMode(PortletMode.VIEW);
		}
		catch (WindowStateException e) { e.printStackTrace(); }
		catch (PortletModeException e) { e.printStackTrace(); }
		return portletURL.toString();
	}

    public StreamedContent putFileContentToResponseOutputStream(byte[] bytes, String contentType, String fileName) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        if (bytes == null || bytes.length == 0) {
            ec.setResponseStatus(204); // No content
        } else {
            ec.setResponseContentType(contentType); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ExternalContext#getMimeType() for auto-detection based on filename.
            ec.setResponseContentLength(bytes.length); // Set it with the file size. This header is optional. It will work if it's omitted, but the download progress will be unknown.
            ec.setResponseHeader("Content-Disposition", "attachment; filename=" + fileName); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.
            try {
                OutputStream out = ec.getResponseOutputStream();
                out.write(bytes);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        return null;
    }

    public void writeBackup(byte[] bytes, String zipFileName) {
        if (bytes == null || zipFileName == null) return;
        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            Convert.writeBytesToBufferedOutputStream(bytes, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public byte[] createBackupZipContent(List<SerializedObjectWrapper> serializedObjectWrappers) {
        byte[] result = null;
        if (serializedObjectWrappers == null) return result;
        byte[] buffer = new byte[1024];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);

            ZipEntry ze;
            InputStream is;
            int len;

            for (SerializedObjectWrapper sow : serializedObjectWrappers) {
                ze = new ZipEntry(sow.tag);
                zos.putNextEntry(ze);
                is = new ByteArrayInputStream(sow.bytes);
                while ((len = is.read(buffer)) > 0) zos.write(buffer, 0, len);
                is.close();
            }
            zos.closeEntry();
            zos.close();

            result = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<SerializedObjectWrapper> readBackup(String zipFileName) {
        List<SerializedObjectWrapper> result = new ArrayList<SerializedObjectWrapper>();
        if (zipFileName == null) return result;
        try {
            FileInputStream fis = new FileInputStream(zipFileName);
            return readBackup(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<SerializedObjectWrapper> readBackup(byte[] zipFileContent) {
        List<SerializedObjectWrapper> result = new ArrayList<SerializedObjectWrapper>();
        if (zipFileContent == null) return result;
        ByteArrayInputStream bis = new ByteArrayInputStream(zipFileContent);
        return readBackup(bis);
    }

    public List<SerializedObjectWrapper> readBackup(InputStream isZipFileContent) {
        List<SerializedObjectWrapper> result = new ArrayList<SerializedObjectWrapper>();
        try {
            ZipInputStream zis = new ZipInputStream(isZipFileContent);

            ZipEntry ze;
            ByteArrayOutputStream bos;
            byte[] buffer = new byte[1024];

            ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();

                bos = new ByteArrayOutputStream();
                int len = 0;
                while ((len = zis.read(buffer)) > 0) bos.write(buffer, 0, len);
                bos.close();

                SerializedObjectWrapper sow = new SerializedObjectWrapper();
                sow.className = "entity.Report";
                sow.classSimpleName = "Report";
                sow.tag = fileName;
                sow.bytes = bos.toByteArray();
                result.add(sow);

                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] createExcelFilesZipContent(List<FileWrapper> fileWrappers) {
        byte[] result = null;
        if (fileWrappers == null) return result;
        byte[] buffer = new byte[1024];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);

            ZipEntry ze;
            InputStream is;
            int len;

            for (FileWrapper fw : fileWrappers) {
                ze = new ZipEntry(fw.getFileName());
                zos.putNextEntry(ze);
                is = new ByteArrayInputStream(fw.getBytes());
                while ((len = is.read(buffer)) > 0) zos.write(buffer, 0, len);
                is.close();
            }
            zos.closeEntry();
            zos.close();

            result = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] createExcelFilesZipContentByFileList(List<AttachedFile> attachedFileList) {
        byte[] result = null;
        if (attachedFileList == null) return result;
        byte[] buffer = new byte[1024];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
            zos.setEncoding("CP866");

            ZipArchiveEntry ze;
            InputStream is;
            int len;

            for (AttachedFile rf : attachedFileList) {
                ze = new ZipArchiveEntry(rf.getFileName());
                zos.putArchiveEntry(ze);
                is = new ByteArrayInputStream(rf.getFile());
                while ((len = is.read(buffer)) > 0) zos.write(buffer, 0, len);
                is.close();
            }
            zos.closeArchiveEntry();
            zos.close();

            result = bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getApplicationProperties() {
        StringBuilder sb = new StringBuilder();

        sb.append("<table class=\"dataTable\">");
        sb.append("<tr><th>Свойство</th><th>Значение</th><tr>");
        sb.append("<tr><td>").append("ORB server").append("</td><td>").append(orbHost + ":" + orbPort).append("</td><tr>");
        sb.append("<tr><td>&#160;</td><td></td><tr>");
        sb.append("<tr><td>").append("Liferay Faces Bridge version").append("</td><td>").append(getLiferayFacesBridgeVersion()).append("</td><tr>");
        sb.append("<tr><td>").append("Liferay Faces Bridge Response Namespace").append("</td><td>").append(getLiferayFacesResponseNamespace()).append("</td><tr>");
        sb.append("</table>");

        return sb.toString();
    }

    public String getOrbHost() {
        return orbHost;
    }

    public String getTestPort() {
        return testPort;
    }
}
