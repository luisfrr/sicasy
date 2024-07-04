package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.EstatusSiniestro;
import gob.yucatan.sicasy.business.entities.Siniestro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.services.iface.IEstatusSiniestroService;
import gob.yucatan.sicasy.services.iface.ISiniestroService;
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

    // Servicios/Beans
    private final ISiniestroService siniestroService;
    private final IEstatusSiniestroService estatusSiniestroService;

    // Constantes
    private final @Getter String SINIESTRO_RESPONSABLE_ASEGURADO = "ASEGURADO";
    private final @Getter String SINIESTRO_RESPONSABLE_TERCEROS = "TERCEROS";

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter List<Siniestro> siniestroList;
    private @Getter @Setter List<Siniestro> siniestroSelectedList;
    private @Getter @Setter Siniestro siniestroSelected;
    private @Getter @Setter Siniestro siniestroForm;
    private @Getter @Setter Siniestro siniestroFilter;

    // Variables selects
    private @Getter List<EstatusSiniestro> estatusSiniestroList;

    // Variables para renderizar
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
        this.siniestroFilter.setEstatusSiniestro(new EstatusSiniestro());
        this.siniestroList = new ArrayList<>();

        this.loadEstatusSiniestros();

        this.showSiniestroListPanel = true;
        this.siniestroSelected = null;
        this.siniestroForm = null;

        PrimeFaces.current().ajax().update("form_filtros");
    }

    public void buscar() {
        log.info("buscar - SiniestrosView");
        this.siniestroList = siniestroService.findAllDynamic(this.siniestroFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }


    public ExportFile exportarSiniestros() {
        return null;
    }


    //region privates

    private void loadEstatusSiniestros() {
        this.estatusSiniestroList = estatusSiniestroService.findAll();
    }

    //endregion privates

}
