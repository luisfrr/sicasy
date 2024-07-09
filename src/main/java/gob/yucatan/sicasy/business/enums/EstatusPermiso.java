package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum EstatusPermiso {

    SIN_ASIGNAR("Sin asignar"),
    HABILITADO("Habilitado"),
    DESHABILITADO("Deshabilitado");

    private final String label;

    EstatusPermiso(String label) {
        this.label = label;
    }

}
