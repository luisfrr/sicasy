package gob.yucatan.sicasy.views.beans;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

@Component
@Scope("request")
@Slf4j
public class FileBean implements Serializable {

    public DefaultStreamedContent getFile(String filePath) {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            File file = new File(filePath);
            return DefaultStreamedContent.builder()
                    .contentType(externalContext.getMimeType(file.getName()))
                    .stream(() -> {
                        try {
                            return new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }



}
