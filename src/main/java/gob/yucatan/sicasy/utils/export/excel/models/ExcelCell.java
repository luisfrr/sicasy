package gob.yucatan.sicasy.utils.export.excel.models;

import lombok.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ExcelCell {
    private String columnName;
    private String propertyExpression;
    private XSSFCellStyle cellStyle;
    private boolean cellAutoSize;
    private Integer cellWidth;
    private Integer columnStart;
    private Integer columnEnd;
}
