package gob.yucatan.sicasy.business.enums;

import lombok.Getter;

@Getter
public enum TipoPermiso {

    MODULE("Módulo"),
    WRITE("Escritura"),
    READ("Lectura");

    private final String label;

    TipoPermiso(String label) {
        this.label = label;
    }

}
