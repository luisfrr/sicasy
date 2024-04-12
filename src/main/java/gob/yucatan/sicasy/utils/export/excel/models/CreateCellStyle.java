package gob.yucatan.sicasy.utils.export.excel.models;

import lombok.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateCellStyle {

    private XSSFWorkbook workbook;
    private int fontSize;
    private boolean isBold;
    private boolean isItalic;
    private ExcelFontColor fontColor;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private ExcelBackgroundColor backgroundColor;
    private String dataFormat;
    private boolean isWrapText;
}
