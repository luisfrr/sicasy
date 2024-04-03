package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum EstatusRegistro {
    BORRADO("Borrado"),
    ACTIVO("Activo");

    private final String label;

    EstatusRegistro(String label) {
        this.label = label;
    }
}
