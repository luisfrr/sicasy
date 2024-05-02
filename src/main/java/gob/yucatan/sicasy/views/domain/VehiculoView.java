package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.imports.excel.ConfigHeaderExcelModel;
import gob.yucatan.sicasy.utils.imports.excel.ImportExcelFile;
import gob.yucatan.sicasy.views.beans.AppBean;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class VehiculoView implements Serializable {

    private @Getter final String LAYOUT_VEHICULOS = "C:\\sicasy\\files\\layout\\layout_vehiculo.xlsx";
    private @Getter final Integer ESTATUS_VEHICULO_ACTIVO = 1;
    private @Getter final Integer ESTATUS_VEHICULO_REGISTRADO = 2;
    private @Getter final Integer ESTATUS_VEHICULO_POR_AUTORIZAR = 3;
    private @Getter final Integer ESTATUS_VEHICULO_RECHAZADO = 4;
    private @Getter final Integer ESTATUS_VEHICULO_CANCELADO = 5;

    private @Getter String title;
    private @Getter List<Vehiculo> vehiculoList;
    private @Getter List<Vehiculo> vehiculoSelectedList;
    private @Getter List<Vehiculo> vehiculoImportList;
    private @Getter @Setter Vehiculo vehiculoSelected;
    private @Getter @Setter Vehiculo vehiculoFilter;
    private @Getter boolean showNuevoFormDialog;
    private @Getter boolean showErrorImportacion;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;

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
    private @Getter String layoutFileUpload;

    private final AppBean appBean;
    private final UserSessionBean userSessionBean;
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
        PrimeFaces.current().ajax().update("form_filtros");
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

        this.layoutFileUpload = null;
        this.showErrorImportacion = false;
        this.acuseImportacionList = null;

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
        log.info("guardar nuevo vehiculo");
        try {
            if(this.vehiculoSelected != null) {
                this.vehiculoSelected.setCreadoPor(userSessionBean.getUserName());
                this.vehiculoSelected.setFechaCreacion(new Date());
                vehiculoService.agregar(this.vehiculoSelected);
                Messages.addInfo("Se ha registrado correctamente el vehículo");
                this.limpiarFiltros();
                this.buscar();
                this.cerrarModalRegistroVehiculo();
            }
        } catch (Exception e) {
            log.error("Error al guardar nuevo vehiculo", e);
            String message;
            if(e instanceof BadRequestException)
                message = e.getMessage();
            else if(e instanceof NotFoundException)
                message = e.getMessage();
            else
                message = "Ocurrió un error inesperado. Intenta de nuevo más tarde.";
            Messages.addError(message);
        }
    }

    public void guardarImportacionVehiculo() {
        log.info("guardar importacion vehiculo");
        try {
            if(this.vehiculoSelected != null && this.vehiculoImportList != null) {
                this.acuseImportacionList = vehiculoService.importar(this.vehiculoImportList,
                        this.vehiculoSelected.getDependencia().getIdDependencia(),
                        userSessionBean.getUserName());

                // Si alguno marco error entonces no se guardó nada y se muestra el acuse
                if(acuseImportacionList.stream().anyMatch(a -> a.getError() == 1)) {
                    this.showErrorImportacion = true;
                }
                else {
                    // Si no, entonces se guardó correctamente
                    Messages.addInfo("Todos los vehículos se han registrado correctamente");
                    this.limpiarFiltros();
                    this.buscar();
                    this.cerrarModalRegistroVehiculo();
                }
            }
        } catch (Exception e) {
            log.error("Error al guardar nuevo vehiculo", e);
            String message;
            if(e instanceof BadRequestException)
                message = e.getMessage();
            else if(e instanceof NotFoundException)
                message = e.getMessage();
            else
                message = "Ocurrió un error inesperado. Intenta de nuevo más tarde.";
            Messages.addError(message);
        }
    }

    public void importarLayout(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        byte[] fileContent = file.getContent();

        // Aquí puedes procesar el archivo según su tipo o contenido
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            // Si es un archivo Excel, procesarlo utilizando Apache POI
            Class<Vehiculo> vehiculoClass = Vehiculo.class;
            List<ConfigHeaderExcelModel> list = new ArrayList<>();
            list.add(ConfigHeaderExcelModel.builder().header("NO_SERIE").fieldName("noSerie").columnIndex(0).build());
            list.add(ConfigHeaderExcelModel.builder().header("PLACA").fieldName("placa").columnIndex(1).build());
            list.add(ConfigHeaderExcelModel.builder().header("MARCA").fieldName("marca").columnIndex(2).build());
            list.add(ConfigHeaderExcelModel.builder().header("MODELO").fieldName("modelo").columnIndex(3).build());
            list.add(ConfigHeaderExcelModel.builder().header("AÑO").fieldName("anio").columnIndex(4).build());
            list.add(ConfigHeaderExcelModel.builder().header("COLOR").fieldName("color").columnIndex(5).build());
            list.add(ConfigHeaderExcelModel.builder().header("NO_MOTOR").fieldName("noMotor").columnIndex(6).build());
            list.add(ConfigHeaderExcelModel.builder().header("CONDICION_ID").fieldName("condicionId").columnIndex(7).build());
            list.add(ConfigHeaderExcelModel.builder().header("NO_FACTURA").fieldName("noFactura").columnIndex(8).build());
            list.add(ConfigHeaderExcelModel.builder().header("VALOR_FACTURA").fieldName("montoFactura").columnIndex(9).build());
            list.add(ConfigHeaderExcelModel.builder().header("RENTA_MENSUAL").fieldName("rentaMensual").columnIndex(10).build());
            list.add(ConfigHeaderExcelModel.builder().header("DESCRIPCION").fieldName("descripcionVehiculo").columnIndex(11).build());
            list.add(ConfigHeaderExcelModel.builder().header("NUM_LICITACION").fieldName("numLicitacion").columnIndex(12).build());
            list.add(ConfigHeaderExcelModel.builder().header("ANEXO").fieldName("anexoValue").columnIndex(13).build());
            list.add(ConfigHeaderExcelModel.builder().header("RESGUARDANTE").fieldName("resguardante").columnIndex(14).build());
            list.add(ConfigHeaderExcelModel.builder().header("AREA_RESGUARDANTE").fieldName("areaResguardante").columnIndex(15).build());
            list.add(ConfigHeaderExcelModel.builder().header("PROVEEDOR").fieldName("proveedor").columnIndex(16).build());

            ImportExcelFile<Vehiculo> importExcelFile = new ImportExcelFile<>();
            this.vehiculoImportList = importExcelFile.processExcelFile(fileContent, vehiculoClass, list);

            this.layoutFileUpload = fileName;
            PrimeFaces.current().ajax().update("tab-view:layout-form:dropZoneLayout");
            log.info("Se ha cargado la información del layout correctamente. Vehículos a importar: {}", this.vehiculoImportList.size());
        } else {
            // Manejar otros tipos de archivos si es necesario
            // Por ejemplo, mostrar un mensaje de error
            Messages.addError("Error", "Tipo de archivo no válido");
        }
    }

    public void solicitarAutorizacion() {
        try {
            this.cambiarEstatus(ESTATUS_VEHICULO_POR_AUTORIZAR, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void autorizarSolicitud() {
        try {
            this.cambiarEstatus(ESTATUS_VEHICULO_ACTIVO, null);
        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
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

    private void cambiarEstatus(Integer estatusVehiculo, String motivo) {
        if(this.vehiculoSelectedList.isEmpty())
            throw new BadRequestException("No ha seleccionado ningún vehículo.");

        List<Long> idVehiculoList = vehiculoSelectedList.stream()
                .map(Vehiculo::getIdVehiculo)
                .toList();

        if(Objects.equals(estatusVehiculo, ESTATUS_VEHICULO_POR_AUTORIZAR)) {
            vehiculoService.solicitarAutorizacion(idVehiculoList, userSessionBean.getUserName());
        } else if (Objects.equals(estatusVehiculo, ESTATUS_VEHICULO_ACTIVO)) {
            vehiculoService.autorizarSolicitud(idVehiculoList, userSessionBean.getUserName());
        } else if(Objects.equals(estatusVehiculo, ESTATUS_VEHICULO_RECHAZADO)) {
            vehiculoService.rechazarSolicitud(idVehiculoList, motivo, userSessionBean.getUserName());
        } else if(Objects.equals(estatusVehiculo, ESTATUS_VEHICULO_CANCELADO)) {
            vehiculoService.cancelarSolicitud(idVehiculoList, motivo, userSessionBean.getUserName());
        }
    }

    //endregion private methods
}
