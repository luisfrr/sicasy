package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum TipoPermiso {

    VIEW("Vista"),
    CONTROLLER("Controlador"),
    WRITE("Escritura"),
    READ("Lectura");

    private final String label;

    TipoPermiso(String label) {
        this.label = label;
    }

}
