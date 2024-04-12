package gob.yucatan.sicasy.utils.export.excel.services.iface;

import edu.umm.platform.utils.export.ExportFile;
import edu.umm.platform.utils.export.excel.models.CreateCellStyle;
import edu.umm.platform.utils.export.excel.models.ExcelDataSheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;

public interface IGeneratorExcelFile {

    XSSFWorkbook createWorkbook();
    ExportFile createExcelFile(XSSFWorkbook workbook, ExcelDataSheet excelDataSheet) throws IOException;
    XSSFCellStyle createCellStyle(CreateCellStyle createCellStyle);

}
