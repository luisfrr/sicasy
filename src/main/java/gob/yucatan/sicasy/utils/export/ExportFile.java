package gob.yucatan.sicasy.utils.export;


import lombok.*;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExportFile {

    private ByteArrayOutputStream stream;
    private ExportFileType exportFileType;
    private String fileName;

    public String getUniqueFileName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return this.fileName.trim() + "_" + dateFormat.format(new Date()) + this.exportFileType.getExtension();
    }

}
