package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum EstatusPermiso {

    BORRADO("Borrado"),
    HABILITADO("Habilitado"),
    DESHABILITADO("Deshabilitado");

    private final String label;

    EstatusPermiso(String label) {
        this.label = label;
    }

}
