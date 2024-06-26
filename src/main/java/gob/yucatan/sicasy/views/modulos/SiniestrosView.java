package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Siniestro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.utils.export.ExportFile;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SINIESTRO_VIEW",
        nombre = "Módulo de Siniestros", descripcion = "Permite ver y filtrar la información de los siniestros.",
        url = "/views/modulos/siniestros.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_VIEW')")
public class SiniestrosView implements Serializable {

    // Constantes

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter List<Siniestro> siniestroList;
    private @Getter @Setter Siniestro siniestroSelected;
    private @Getter @Setter Siniestro siniestroFilter;


    // Render Paneles y Dialogs
    private @Getter boolean showSiniestroListPanel;
    private @Getter boolean showNuevoSiniestroPanel;


    @PostConstruct
    public void init() {
        log.info("init - SiniestrosView");
        this.title = "Siniestros";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros - SiniestrosView");
        this.siniestroSelected = null;
        this.siniestroFilter = new Siniestro();
        this.siniestroList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros");
    }






    public ExportFile exportarSiniestros() {
        return null;
    }


}
