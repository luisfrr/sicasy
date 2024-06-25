package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SINIESTRO_VIEW",
        nombre = "Módulo de Siniestros", descripcion = "Permite ver y filtrar la información de los siniestros.",
        url = "/views/modulos/siniestros.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_VIEW')")
public class SiniestrosView implements Serializable {
}
