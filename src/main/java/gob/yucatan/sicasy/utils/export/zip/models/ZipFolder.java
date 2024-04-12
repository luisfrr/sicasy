package gob.yucatan.sicasy.utils.export.zip.models;

import gob.yucatan.sicasy.utils.export.ExportFile;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ZipFolder {
    private String folderName;
    private List<ExportFile> fileList;
    private List<ZipFolder> subFolderList;
}
