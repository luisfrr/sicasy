package gob.yucatan.sicasy.utils.export;

import lombok.Getter;

@Getter
public enum ExportFileType {
    PNG("image/png", ".png"),
    JPG("image/jpeg", ".jpg"),
    JPEG("image/jpeg", ".jpeg"),

    CSV("text/csv", ".csv"),
    XML("text/xml", ".xml"),
    TEXT("application/text", ".txt"),
    JSON("application/json", ".json"),
    PDF("application/pdf", ".pdf"),
    RAR("application/x-rar-compressed", ".rar"),
    ZIP("application/zip", ".zip"),

    MS_WORD_DOC("application/msword", ".doc"),
    MS_WORD_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    MS_EXCEL_XLS("application/vnd.ms-excel", ".xls"),
    MS_EXCEL_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    MS_POWERPOINT_PPT("application/vnd.ms-powerpoint", ".ppt"),
    MS_POWERPOINT_PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"),

    ;

    private final String contentType;
    private final String extension;

    ExportFileType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }
}
