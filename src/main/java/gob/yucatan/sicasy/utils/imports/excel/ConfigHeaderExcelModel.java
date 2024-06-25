package gob.yucatan.sicasy.utils.imports.excel;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConfigHeaderExcelModel {
    private String header;
    private String fieldName;
    private int columnIndex;
    private String dateFormat;
}
