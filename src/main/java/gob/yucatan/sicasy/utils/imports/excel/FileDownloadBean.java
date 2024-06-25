package gob.yucatan.sicasy.utils.imports.excel;

import gob.yucatan.sicasy.utils.export.ExportFile;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;

@Slf4j
@Component
@Scope("request")
@Getter
public class FileDownloadBean implements Serializable {

    public DefaultStreamedContent downloadFile(ExportFile exportFile) {
        // Convert ByteArrayOutputStream to ByteArrayInputStream
        ByteArrayInputStream in = new ByteArrayInputStream(exportFile.getStream().toByteArray());

        return DefaultStreamedContent.builder()
                .stream(() -> in)
                .name(exportFile.isUseOriginalFileName() ? exportFile.getFileName() : exportFile.getUniqueFileName())
                .contentType(exportFile.getExportFileType().getContentType())
                .build();
    }

    public DefaultStreamedContent downloadFileByPath(String filePath) {
        log.info("downloadFile: {}", filePath);
        File file = new File(filePath);

        if (!file.exists()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se encontró el archivo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }

        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            String contentType = externalContext.getMimeType(file.getName());
            return DefaultStreamedContent.builder()
                    .name(file.getName())
                    .contentType(contentType)
                    .stream(() -> {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            log.error("File not found: {}", filePath, e);
                            return null;
                        }
                    })
                    .build();
        } catch (Exception e) {
            log.error("Error downloading file: {}", filePath, e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Ocurrió un error al intentar descargar el archivo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
    }

}
