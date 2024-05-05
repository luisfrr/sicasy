package gob.yucatan.sicasy.business.annotations;

import gob.yucatan.sicasy.business.enums.TipoPermiso;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ConfigPermiso {
    TipoPermiso tipo();
    String codigo();
    String nombre();
    String descripcion() default "";
    String url() default "";
    int orden() default 0;
}

