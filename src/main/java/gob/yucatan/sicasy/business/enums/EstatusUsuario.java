package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum EstatusUsuario {
    BORRADO("Borrado"),
    ACTIVO("Activo"),
    REGISTRADO("Registrado"),
    BLOQUEADO("Bloqueado");

    private final String label;

    EstatusUsuario(String label) {
        this.label = label;
    }
}
