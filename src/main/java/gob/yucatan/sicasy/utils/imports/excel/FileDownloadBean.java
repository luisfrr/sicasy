package gob.yucatan.sicasy.utils.imports.excel;

import gob.yucatan.sicasy.utils.export.ExportFile;
import lombok.Getter;
import org.primefaces.model.DefaultStreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

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

}
