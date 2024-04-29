package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.export.ExportFile;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class VehiculoView implements Serializable {

    private @Getter String title;
    private @Getter List<Vehiculo> vehiculoList;
    private @Getter List<Vehiculo> vehiculoSelectedList;
    private @Getter @Setter Vehiculo vehiculoSelected;
    private @Getter @Setter Vehiculo vehiculoFilter;
    private @Getter boolean showNuevoFormDialog;

    private @Getter List<Dependencia> dependenciaList;
    private @Getter List<CondicionVehiculo> condicionVehiculoList;
    private @Getter List<EstatusVehiculo> estatusVehiculoList;
    private @Getter List<Licitacion> licitacionList;
    private @Getter List<Anexo> anexoList;
    private @Getter List<String> marcaList;
    private @Getter List<String> modeloList;
    private @Getter List<Integer> anioList;

    private @Getter List<Licitacion> licitacionFormList;
    private @Getter List<Anexo> anexoFormList;
    private @Getter ExportFile layoutVehiculo;

    private final IVehiculoService vehiculoService;
    private final IDependenciaService dependenciaService;
    private final ICondicionVehiculoService condicionVehiculoService;
    private final IEstatusVehiculoService estatusVehiculoService;
    private final ILicitacionService licitacionService;
    private final IAnexoService anexoService;


    @PostConstruct
    public void init() {
        log.info("Inicializando VehiculoView");
        this.title = "Vehículo";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiar filtros de vehiculos");
        this.vehiculoSelected = null;
        this.vehiculoFilter = new Vehiculo();
        this.vehiculoFilter.setDependencia(new Dependencia());
        this.vehiculoFilter.setCondicionVehiculo(new CondicionVehiculo());
        this.vehiculoFilter.setEstatusVehiculo(new EstatusVehiculo());
        this.vehiculoFilter.setLicitacion(new Licitacion());
        this.vehiculoFilter.setAnexo(new Anexo());

        this.loadDependencias();
        this.loadCondicionVehiculos();
        this.loadEstatusVehiculos();
        this.loadLicitaciones();
        this.loadAnexos();
        this.loadMarcas();
        this.loadModelos();
        this.loadTipoVehiculo();

        this.showNuevoFormDialog = false;

        this.vehiculoList = new ArrayList<>();
        this.vehiculoSelectedList = new ArrayList<>();
    }

    public void buscar() {
        log.info("buscar vehiculos");
        this.vehiculoList = vehiculoService.findAllDynamic(this.vehiculoFilter);
    }

    public void abrirModalRegistroVehiculo() {
        log.info("abrir modal registro vehiculo");
        this.showNuevoFormDialog = true;
        this.vehiculoSelected = new Vehiculo();
        this.vehiculoSelected.setDependencia(new Dependencia());
        this.vehiculoSelected.setDependenciaAsignada(new Dependencia());
        this.vehiculoSelected.setCondicionVehiculo(new CondicionVehiculo());
        this.vehiculoSelected.setEstatusVehiculo(new EstatusVehiculo());
        this.vehiculoSelected.setLicitacion(new Licitacion());
        this.vehiculoSelected.setAnexo(new Anexo());

        this.loadLicitacionesForm();
        this.loadAnexosForm();

        PrimeFaces.current().ajax().update("registrar-vehiculo-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarVehiculoDialog').show()");
    }

    public void cerrarModalRegistroVehiculo() {
        log.info("cerrar modal registro vehiculo");
        this.showNuevoFormDialog = false;
        this.vehiculoSelected = null;
        PrimeFaces.current().ajax().update("registrar-vehiculo-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarVehiculoDialog').hide()");
    }

    public void guardarNuevoVehiculo() {

    }

    public void verDetalle(Vehiculo vehiculo) {
        log.info("ver detalle vehiculos");
        this.vehiculoSelected = vehiculo;
    }

    public void abrirModalRegistroMantenimiento(Long idVehiculo) {
        log.info("abrir modal registro mantenimiento");
        this.vehiculoSelected = vehiculoService.findById(idVehiculo);
    }

    public void abrirModalAdjuntarFotos(Long idVehiculo) {
        log.info("abrir modal adjuntar fotos");
        this.vehiculoSelected = vehiculoService.findById(idVehiculo);
    }


    //region Events

    public void onChangeLicitacionFilter() {
        this.loadAnexos();
    }

    public void onChangeMarcaFilter() {
        this.loadModelos();
    }

    public void onChangeModeloFilter() {
        this.loadTipoVehiculo();
    }

    public void onChangeLicitacionForm() {
        this.loadAnexosForm();
    }

    //endregion Events

    //region private methods

    private void loadDependencias() {
        this.dependenciaList = dependenciaService.findAll();
    }

    private void loadCondicionVehiculos() {
        this.condicionVehiculoList = condicionVehiculoService.findAll();
    }

    private void loadEstatusVehiculos() {
        this.estatusVehiculoList = estatusVehiculoService.findAll();
    }

    private void loadLicitaciones() {
        List<Licitacion> licitaciones = licitacionService.findAllLicitacionActive();
        if(licitaciones.isEmpty())
            this.licitacionList = new ArrayList<>();
        else
            this.licitacionList = licitaciones.stream()
                    .sorted(Comparator.comparing(Licitacion::getNumeroLicitacion))
                    .toList();
    }

    private void loadAnexos() {
        if(this.vehiculoFilter.getLicitacion() != null && this.vehiculoFilter.getLicitacion().getIdLicitacion() != null) {
            List<Anexo> anexos = anexoService.findByLicitacion(this.vehiculoFilter.getLicitacion());
            if(anexos.isEmpty())
                this.anexoList = new ArrayList<>();
            else
                this.anexoList = anexos.stream()
                        .sorted(Comparator.comparing(Anexo::getNombre))
                        .toList();
        } else {
            this.anexoList = new ArrayList<>();
        }
    }

    private void loadMarcas() {
        this.marcaList = vehiculoService.findDistinctMarcas();
    }

    private void loadModelos() {
        if(this.vehiculoFilter.getMarca() != null && !this.vehiculoFilter.getMarca().isEmpty()) {
            this.modeloList = vehiculoService.findDistinctModelo(this.vehiculoFilter.getMarca());
        } else {
            this.modeloList = new ArrayList<>();
        }
    }

    private void loadTipoVehiculo() {
        if(this.vehiculoFilter.getMarca() != null && !this.vehiculoFilter.getMarca().isEmpty() &&
                this.vehiculoFilter.getModelo() != null && !this.vehiculoFilter.getModelo().isEmpty() ) {
            this.anioList = vehiculoService.findDistinctAnio(this.vehiculoFilter.getMarca(), this.vehiculoFilter.getModelo());
        } else {
            this.anioList = new ArrayList<>();
        }
    }

    private void loadLicitacionesForm() {
        List<Licitacion> licitaciones = licitacionService.findAllLicitacionActive();
        if(licitaciones.isEmpty())
            this.licitacionFormList = new ArrayList<>();
        else
            this.licitacionFormList = licitaciones.stream()
                    .sorted(Comparator.comparing(Licitacion::getNumeroLicitacion))
                    .toList();
    }

    private void loadAnexosForm() {
        if(this.vehiculoSelected.getLicitacion() != null && this.vehiculoSelected.getLicitacion().getIdLicitacion() != null) {
            List<Anexo> anexos = anexoService.findByLicitacion(this.vehiculoSelected.getLicitacion());
            if(anexos.isEmpty())
                this.anexoFormList = new ArrayList<>();
            else
                this.anexoFormList = anexos.stream()
                        .sorted(Comparator.comparing(Anexo::getNombre))
                        .toList();
        } else {
            this.anexoFormList = new ArrayList<>();
        }
    }



    //endregion private methods
}
