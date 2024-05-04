package gob.yucatan.sicasy.views.beans;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

public class Messages {

    public static void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
        PrimeFaces.current().ajax().update("growl");
    }

    public static void addInfo(String summary, String detail) {
        addMessage(FacesMessage.SEVERITY_INFO, summary, detail);
    }

    public static void addInfo(String detail) {
        addMessage(FacesMessage.SEVERITY_INFO, "¡Todo listo!", detail);
    }

    public static void addError(String summary, String detail) {
        addMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
    }

    public static void addError(String detail) {
        addMessage(FacesMessage.SEVERITY_ERROR, "Error", detail);
    }

    public static void addWarn(String summary, String detail) {
        addMessage(FacesMessage.SEVERITY_WARN, summary, detail);
    }

    public static void addWarn(String detail) {
        addMessage(FacesMessage.SEVERITY_WARN, "Advertencia", detail);
    }

}
