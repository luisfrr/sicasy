package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.GrupoPoliza;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class PolizaView implements Serializable {

    @Value("${app.files.folder.layouts.importar-vehiculo}")
    private @Getter String LAYOUT_POLIZAS;

    @Value("${app.files.folder.polizas}")
    private @Getter String FOLDER_POLIZAS;

    // Generales
    private @Getter String title;
    private @Getter Poliza polizaFilter;
    private @Getter GrupoPoliza grupoPolizaSelected;
    private @Getter List<GrupoPoliza> grupoPolizaList;
    private @Getter List<Poliza> polizaList;

    private @Getter boolean showPanelPolizas;

    private final IPolizaService polizaService;


    @PostConstruct
    public void init() {
        this.title = "Polizas";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiar filtros de vehiculos");
        this.grupoPolizaSelected = null;
        this.polizaFilter = new Poliza();
        this.polizaFilter.setAseguradora(new Aseguradora());
        this.polizaFilter.setEstatusPoliza(new EstatusPoliza());
        this.polizaFilter.setVehiculo(new Vehiculo());

        this.showPanelPolizas = true;

        this.grupoPolizaList = new ArrayList<>();
        this.polizaList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros", "form_datatable");
    }

    public void buscar() {
        log.info("buscar grupos polizas");
        this.grupoPolizaList = polizaService.findGrupoPoliza(this.polizaFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }


}
