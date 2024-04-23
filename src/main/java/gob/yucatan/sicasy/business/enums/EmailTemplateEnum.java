package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateEnum {
    NO_TEMPLATE("NO_TEMPLATE"),
    SIMPLE_TEMPLATE("SIMPLE_TEMPLATE");

    private final String codigo;

    EmailTemplateEnum(String codigo) {
        this.codigo = codigo;
    }

}
