package gob.yucatan.sicasy.utils.export.zip.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ZipDataModel {
    private ZipFolder mainZipFolder;
    private String filename;
}
