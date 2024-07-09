package gob.yucatan.sicasy.utils.export.excel.services.impl;

import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.ExportFileType;
import gob.yucatan.sicasy.utils.export.excel.models.*;
import gob.yucatan.sicasy.utils.export.excel.services.iface.IGeneratorExcelFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class GeneratorExcelFileImpl implements IGeneratorExcelFile {

    @Override
    public XSSFWorkbook createWorkbook() {
        return new XSSFWorkbook();
    }

    @Override
    public ExportFile createExcelFile(XSSFWorkbook workbook, ExcelDataSheet excelDataSheet) throws IOException {

        // Se crea la hoja
        XSSFSheet sheet = workbook.createSheet(excelDataSheet.getSheetName());

        // Contador de filas
        int rowNumber = 0;
        int rowHeader;

        // Se agrega la celda de título y subtítulo
        rowNumber = addTitle(workbook, sheet, excelDataSheet.getTitle(), excelDataSheet.getCells().size(), rowNumber);
        rowNumber = addSubtitle(workbook, sheet, excelDataSheet.getSubtitle(), excelDataSheet.getCells().size(), rowNumber);

        // Se agregan las cabeceras
        rowHeader = addHeader(workbook, sheet, excelDataSheet.getCells(), rowNumber);
        rowNumber = rowHeader;

        // Se agregan los datos al excel
        rowNumber = writeData(sheet, excelDataSheet.getData(), excelDataSheet.getCells(), excelDataSheet.getRowResult(), rowNumber);

        // Aplicar filtro a las celdas de cabecera
        if(excelDataSheet.isAutoFilter())
            sheet.setAutoFilter(new CellRangeAddress(rowHeader,rowHeader,0,excelDataSheet.getCells().size() -1));

        // Se agrega la fecha de generación del excel
        if(excelDataSheet.isAgregarFechaGeneracion())
            addGeneratedDate(workbook, sheet, excelDataSheet.getCells().size(), excelDataSheet.getAppName(), rowNumber);

        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);

        return ExportFile.builder()
                .stream(outByteStream)
                .exportFileType(ExportFileType.MS_EXCEL_XLSX)
                .fileName(excelDataSheet.getFilename())
                .build();
    }

    @Override
    public XSSFCellStyle createCellStyle(CreateCellStyle createCellStyle) {
        XSSFFont font = getFont(createCellStyle.getWorkbook(),
                createCellStyle.getFontSize(),
                createCellStyle.isBold(),
                createCellStyle.isItalic(),
                createCellStyle.getFontColor());
        return generateCellStyle(createCellStyle.getWorkbook(),
                font,
                createCellStyle.getHorizontalAlignment(),
                createCellStyle.getVerticalAlignment(),
                createCellStyle.getBackgroundColor(),
                createCellStyle.getDataFormat(),
                createCellStyle.isWrapText());
    }


    private int addTitle(XSSFWorkbook workbook, XSSFSheet sheet, String title, int totalColumns, int rowNumber) {
        if(title != null && !title.isEmpty()) {
            // Estilo del título
            XSSFCellStyle style = this.createCellStyle(CreateCellStyle.builder()
                            .workbook(workbook)
                            .fontSize(16)
                            .isBold(true)
                            .isItalic(false)
                            .fontColor(ExcelFontColor.DARK_BLUE)
                            .horizontalAlignment(HorizontalAlignment.LEFT)
                            .verticalAlignment(VerticalAlignment.CENTER)
                            .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                            .dataFormat(null)
                            .isWrapText(true)
                    .build());

            // Se asigna rowNumber = 2
            rowNumber = 2;

            // Se crea el row
            XSSFRow row = sheet.createRow(rowNumber);
            createCellAutoSize(sheet, row, 0, title, style);
            sheet.addMergedRegion(new CellRangeAddress(rowNumber,rowNumber,0,totalColumns -1));
            row.setHeight((short) (22 * 20));
        }
        return rowNumber;
    }

    private int addSubtitle(XSSFWorkbook workbook, XSSFSheet sheet, String subtitle, int totalColumns, int rowNumber) {
        if(subtitle != null && !subtitle.isEmpty()) {
            // Estilo del subtítulo
            XSSFCellStyle style = this.createCellStyle(CreateCellStyle.builder()
                    .workbook(workbook)
                    .fontSize(14)
                    .isBold(false)
                    .isItalic(false)
                    .fontColor(ExcelFontColor.DARK_BLUE)
                    .horizontalAlignment(HorizontalAlignment.LEFT)
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                    .dataFormat(null)
                    .isWrapText(true)
                    .build());

            // Se crea el row
            XSSFRow row = sheet.createRow(++rowNumber);
            createCellAutoSize(sheet, row, 0, subtitle, style);
            sheet.addMergedRegion(new CellRangeAddress(rowNumber,rowNumber,0,totalColumns -1));
            row.setHeight((short) (20 * 20));
        }
        return rowNumber;
    }

    private int addHeader(XSSFWorkbook workbook, XSSFSheet sheet, List<ExcelCell> cells, int rowNumber) {
        // Estilo del header
        XSSFCellStyle style = this.createCellStyle(CreateCellStyle.builder()
                .workbook(workbook)
                .fontSize(12)
                .isBold(false)
                .isItalic(false)
                .fontColor(ExcelFontColor.BLACK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .backgroundColor(ExcelBackgroundColor.LIGHT_BLUE)
                .dataFormat(null)
                .isWrapText(false)
                .build());

        // Si es difente de 0 entonces se agrega la cabecera en el siguiente row
        if(rowNumber != 0)
            rowNumber = rowNumber + 2;

        // Se crea el row
        XSSFRow row = sheet.createRow(rowNumber);

        int columnCount = 0;
        for (ExcelCell cell : cells) {
            createCellAutoSize(sheet, row, columnCount, cell.getColumnName(), style);
            columnCount++;
        }

        row.setHeight((short) (18 * 20));

        return rowNumber;
    }

    private int writeData(XSSFSheet sheet, List<?> data, List<ExcelCell> cells, ExcelRowResult rowResult, int rowNumber) {

        for(Object item : data) {

            int columnCount = 0;

            // Se crea el row
            XSSFRow row = sheet.createRow(++rowNumber);

            for(ExcelCell cell : cells) {
                Object value;

                SpelExpressionParser parser = new SpelExpressionParser();
                Expression expression = parser.parseExpression(cell.getPropertyExpression());

                value = expression.getValue(item);

                if(cell.getCellWidth() != null && cell.getCellWidth() > 0)
                    createCellFixedSize(sheet, row, columnCount, value, cell.getCellStyle(), cell.getCellWidth());
                else if(cell.isCellAutoSize())
                    createCellAutoSize(sheet, row, columnCount, value, cell.getCellStyle());
                else
                    createCell(row, columnCount, value, cell.getCellStyle());

                columnCount++;
            }
        }

        if(rowResult != null && rowResult.getResult() != null) {

            int columnCount = 0;
            XSSFRow row = sheet.createRow(rowNumber);

            for(ExcelCell cell : rowResult.getCells()) {
                Object value;

                if(cell.getPropertyExpression() != null){
                    SpelExpressionParser parser = new SpelExpressionParser();
                    Expression expression = parser.parseExpression(cell.getPropertyExpression());

                    value = expression.getValue(rowResult.getResult());
                } else {
                    value = cell.getColumnName();
                }

                if(cell.getCellWidth() != null && cell.getCellWidth() > 0)
                    createCellFixedSize(sheet, row, columnCount, value, cell.getCellStyle(), cell.getCellWidth());
                else if(cell.isCellAutoSize())
                    createCellAutoSize(sheet, row, columnCount, value, cell.getCellStyle());
                else
                    createCell(row, columnCount, value, cell.getCellStyle());

                if(cell.getColumnStart() != null && cell.getColumnEnd() != null) {
                    sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, columnCount, cell.getColumnEnd()));
                    columnCount = cell.getColumnEnd();
                }

                columnCount++;
            }

            if(rowResult.getRowHeight() != null) {
                row.setHeight((short) (rowResult.getRowHeight() * 20));
            }
        }
        return rowNumber;

    }

    private void addGeneratedDate(XSSFWorkbook workbook, XSSFSheet sheet, int totalColumns, String appName, int rowNumber) {
        // Estilo del header
        XSSFCellStyle style = this.createCellStyle(CreateCellStyle.builder()
                .workbook(workbook)
                .fontSize(10)
                .isBold(false)
                .isItalic(false)
                .fontColor(ExcelFontColor.RED)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.CENTER)
                .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                .dataFormat(null)
                .isWrapText(false)
                .build());

        rowNumber = rowNumber + 5;
        Date today = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'del' yyyy 'a las' hh:mm a");

        String generatedDate;
        if(appName != null && !appName.isEmpty())
            generatedDate = "Archivo generado desde " + appName + " el día " + dateFormat.format(today);
        else
            generatedDate = "Archivo generado el día " + dateFormat.format(today);

        XSSFRow rowGeneratedDate = sheet.createRow(rowNumber);
        createCell(rowGeneratedDate, 0, generatedDate, style);
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,0,totalColumns -1));
    }

    private void createCellAutoSize(XSSFSheet sheet, XSSFRow row, int columnCount, Object value, XSSFCellStyle style) {
        this.createCell(row, columnCount, value, style);
        sheet.autoSizeColumn(columnCount);
    }

    private void createCellFixedSize(XSSFSheet sheet, XSSFRow row, int columnCount, Object value, XSSFCellStyle style, Integer cellWidth) {
        this.createCell(row, columnCount, value, style);
        // Establecer el tamaño de la celda en caracteres
        int numberOfChars = 20; // Tamaño deseado
        int widthUnits = cellWidth * numberOfChars;
        sheet.setColumnWidth(columnCount, widthUnits);
    }

    private void createCell(XSSFRow row, int columnCount, Object value, XSSFCellStyle style) {
        Cell cell = row.createCell(columnCount);

        switch (value) {
            case null -> cell.setBlank();
            case Integer i -> cell.setCellValue(i);
            case Long l -> cell.setCellValue(l);
            case Boolean b -> cell.setCellValue(b);
            case Double v -> cell.setCellValue(v);
            case Date date -> cell.setCellValue(date);
            case Character c -> cell.setCellValue(c.toString());
            default -> cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }


    //region Estilos Excel

    private XSSFFont getFont(XSSFWorkbook workbook, int fontSize, boolean isBold, boolean isItalic, ExcelFontColor fontColor) {
        XSSFFont font = workbook.createFont();

        font.setFontHeight((short) (fontSize * 20));
        font.setBold(isBold);
        font.setItalic(isItalic);
        font.setFontName("Arial");

        if(fontColor == null)
            font.setColor(IndexedColors.BLACK.getIndex());
        else if(fontColor == ExcelFontColor.BLACK)
            font.setColor(IndexedColors.BLACK.getIndex());
        else if (fontColor == ExcelFontColor.WHITE)
            font.setColor(IndexedColors.WHITE.getIndex());
        else if (fontColor == ExcelFontColor.GREY)
            font.setColor(IndexedColors.GREY_25_PERCENT.getIndex());
        else if (fontColor == ExcelFontColor.BLUE)
            font.setColor(IndexedColors.BLUE.getIndex());
        else if (fontColor == ExcelFontColor.BLUE_GREY)
            font.setColor(IndexedColors.BLUE_GREY.getIndex());
        else if (fontColor == ExcelFontColor.DARK_BLUE)
            font.setColor(IndexedColors.DARK_BLUE.getIndex());
        else if (fontColor == ExcelFontColor.RED)
            font.setColor(IndexedColors.RED.getIndex());
        else if (fontColor == ExcelFontColor.DARK_RED)
            font.setColor(IndexedColors.DARK_RED.getIndex());

        return font;
    }

    private XSSFCellStyle generateCellStyle(XSSFWorkbook workbook, XSSFFont font, HorizontalAlignment horizontalAlignment,
                                        VerticalAlignment verticalAlignment, ExcelBackgroundColor backgroundColor,
                                        String dataFormat, boolean isWrapText) {
        // Se crea el formato
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        // Se agrega la fuente
        cellStyle.setFont(font);

        // Se configura la alineación
        cellStyle.setAlignment(Objects.requireNonNullElse(horizontalAlignment, HorizontalAlignment.LEFT));

        // Se configura la alineación vertical
        cellStyle.setVerticalAlignment(Objects.requireNonNullElse(verticalAlignment, VerticalAlignment.TOP));

        // Se configura el color de fondo de la celda
        if(backgroundColor != null && backgroundColor != ExcelBackgroundColor.NO_BG_COLOR) {

            if(backgroundColor == ExcelBackgroundColor.BLACK)
                cellStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.BLUE)
                cellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.DARK_BLUE)
                cellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.BLUE_GREY)
                cellStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.RED)
                cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.DARK_RED)
                cellStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            else if(backgroundColor == ExcelBackgroundColor.LIGHT_BLUE)
                cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());

            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        // Se configura el formato de la celda
        if(dataFormat != null && !dataFormat.isEmpty())
            cellStyle.setDataFormat(workbook.createDataFormat().getFormat(dataFormat));

        // Se configura el wrapText
        if(isWrapText)
            cellStyle.setWrapText(true);

        return cellStyle;
    }

    //endregion Estilos Excel



}
