package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.imports.excel.ConfigHeaderExcelModel;
import gob.yucatan.sicasy.utils.imports.excel.ImportExcelFile;
import gob.yucatan.sicasy.utils.imports.excel.SaveFile;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.files.folder.layouts.importar-vehiculo}")
    private @Getter String LAYOUT_VEHICULOS;

    @Value("${app.files.folder.vehiculos}")
    private @Getter String FOLDER_VEHICULOS;

    private @Getter final Integer ACCION_RECHAZAR_SOLICITUD = 3;
    private @Getter final Integer ACCION_CANCELAR_SOLICITUD = 4;
    private @Getter final Integer ACCION_SOLICITAR_BAJA = 5;
    private @Getter final Integer ACCION_SOLICITAR_MODIFICACION = 6;

    // Generales
    private @Getter String title;
    private @Getter @Setter List<Vehiculo> vehiculoList;
    private @Getter @Setter List<Vehiculo> vehiculoSelectedList;
    private @Getter @Setter List<Vehiculo> vehiculoImportList;
    private @Getter @Setter Vehiculo vehiculoSelected;
    private @Getter @Setter Vehiculo vehiculoFilter;
    private @Getter boolean showVehiculosPanel; // Seccion completa de filtrado

    // Registro nuevos
    private @Getter boolean showNuevoFormDialog;
    private @Getter boolean showErrorImportacion;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;

    // Seccion completa de detalles del vehículo
    private @Getter boolean showDetailsPanel;
    private @Getter boolean readOnlyEditForm;

    // Se usa para los filtros
    private @Getter List<Dependencia> dependenciaList;
    private @Getter List<CondicionVehiculo> condicionVehiculoList;
    private @Getter List<EstatusVehiculo> estatusVehiculoList;
    private @Getter List<Licitacion> licitacionList;
    private @Getter List<Anexo> anexoList;
    private @Getter List<String> marcaList;
    private @Getter List<String> modeloList;
    private @Getter List<Integer> anioList;

    // Se usa para registros nuevos y edición
    private @Getter List<Licitacion> licitacionFormList;
    private @Getter List<Anexo> anexoFormList;
    private @Getter String layoutFileUpload;

    // Se usan para confirmacion de cambio de estatus
    private @Getter boolean showConfirmEstatus;
    private @Getter Integer confirmAccion;
    private @Getter String confirmMensaje;
    private @Getter @Setter String confirmMotivo;

    // Se usa para adjuntar fotos
    private @Getter boolean showAdjuntarFotos;
    private @Getter Vehiculo vehiculoFotoSelected;

    private final UserSessionBean userSessionBean;
    private final IVehiculoService vehiculoService;
    private final IDependenciaService dependenciaService;
    private final ICondicionVehiculoService condicionVehiculoService;
    private final IEstatusVehiculoService estatusVehiculoService;
    private final ILicitacionService licitacionService;
    private final IAnexoService anexoService;
    private final IVehiculoFotoService vehiculoFotoService;


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

        this.showVehiculosPanel = true;
        this.showDetailsPanel = false;
        this.showNuevoFormDialog = false;

        this.showConfirmEstatus = false;
        this.showAdjuntarFotos = false;

        this.vehiculoList = new ArrayList<>();
        this.vehiculoSelectedList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros");
    }

    public void buscar() {
        log.info("buscar vehiculos");
        this.vehiculoList = vehiculoService.findAllDynamic(this.vehiculoFilter);
        PrimeFaces.current().ajax().update("form_datatable");
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
        log.info("Solicitando autorizacion");
        try {
            List<Long> idVehiculoSelectedList =  this.getIdVehiculoSelectedList();
            vehiculoService.solicitarAutorizacion(idVehiculoSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se ha enviado la solicitud de autorización");
        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void autorizarSolicitud() {
        log.info("Autorizando solicitud");
        try {
            List<Long> idVehiculoSelectedList =  this.getIdVehiculoSelectedList();
            vehiculoService.autorizarSolicitud(idVehiculoSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se han autorizado correctamente las solicitudes");
        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void abrirConfirmEstatusDialog(Integer confirmAccion) {
        log.info("Abrir Confirm Estatus");
        this.showConfirmEstatus = true;
        this.confirmAccion = confirmAccion;
        this.confirmMotivo = "";

        if(Objects.equals(confirmAccion, ACCION_RECHAZAR_SOLICITUD)) {
            this.confirmMensaje = "¿Está seguro que desea rechazar las solicitudes? Esto regresa el estatus del vehiculo a REGISTRADO.";
        } else if (Objects.equals(confirmAccion, ACCION_CANCELAR_SOLICITUD)) {
            this.confirmMensaje = "¿Está seguro que desea cancelar las solicitudes? Una vez canceladas ya no podrás visualizar estos registros.";
        } else if (Objects.equals(confirmAccion, ACCION_SOLICITAR_BAJA)) {
            this.confirmMensaje = "¿Está seguro que desea solicitar la baja de los vehículos? Una vez dados de baja, solo se muestran en reportes históricos.";
        } else if (Objects.equals(confirmAccion, ACCION_SOLICITAR_MODIFICACION)) {
            this.confirmMensaje = "¿Está seguro que desea solicitar la baja de los vehículos? Una vez dados de baja, solo se muestran en reportes históricos.";
        }

        PrimeFaces.current().ajax().update("confirm-estatus-dialog");
        PrimeFaces.current().executeScript("PF('confirmEstatusDialog').show()");
    }

    public void confirmarEstatusDialog() {
        log.info("confirmar estatus dialog");
        try {
            List<Long> idVehiculoSelectedList =  this.getIdVehiculoSelectedList();

            boolean success = false;
            if(Objects.equals(this.confirmAccion, ACCION_RECHAZAR_SOLICITUD)) {
                vehiculoService.rechazarSolicitud(idVehiculoSelectedList, this.confirmMotivo, userSessionBean.getUserName());
                Messages.addInfo("Se han rechazado correctamente las solicitudes de los vehículos seleccionados");
                success = true;
            } else if (Objects.equals(this.confirmAccion, ACCION_CANCELAR_SOLICITUD)) {
                vehiculoService.cancelarSolicitud(idVehiculoSelectedList, this.confirmMotivo, userSessionBean.getUserName());
                Messages.addInfo("Se han cancelado correctamente los registros de los vehículos seleccionados");
                success = true;
            } else if (Objects.equals(this.confirmAccion, ACCION_SOLICITAR_BAJA)) {
                vehiculoService.solicitarBaja(idVehiculoSelectedList, this.confirmMotivo, userSessionBean.getUserName());
                Messages.addInfo("Se han dado de baja los vehículos seleccionados");
                success = true;
            } else if (Objects.equals(this.confirmAccion, ACCION_SOLICITAR_MODIFICACION)) {
                vehiculoService.solicitarModificacion(idVehiculoSelectedList, this.confirmMotivo, userSessionBean.getUserName());
                Messages.addInfo("Se ha solicitado la modificación de los vehículos seleccionados.");
                success = true;
            }

            if(success)
                this.buscar();

        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void cerrarConfirmEstatusDialog() {
        log.info("Cerrar Confirm Estatus");
        this.showConfirmEstatus = false;
        this.confirmAccion = null;
        this.confirmMotivo = "";
        this.confirmMensaje = "";
        PrimeFaces.current().ajax().update("confirm-estatus-dialog");
        PrimeFaces.current().executeScript("PF('confirmEstatusDialog').hide()");
    }

    public void verDetalle(Vehiculo vehiculo) {
        log.info("ver detalle vehiculos");
        this.vehiculoSelected = vehiculoService.findFullById(vehiculo.getIdVehiculo());
        this.showVehiculosPanel = false;
        this.showDetailsPanel = true;
        this.readOnlyEditForm = true;

        this.loadLicitacionesForm();
        this.loadAnexosForm();

        PrimeFaces.current().ajax().update("container");
    }

    public void regresar() {
        log.info("regresar vehiculos");
        this.vehiculoSelected = null;
        this.showVehiculosPanel = true;
        this.showDetailsPanel = false;
        PrimeFaces.current().ajax().update("container");
    }

    public void permitirEditar() {
        this.readOnlyEditForm = false;
        PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo");
    }

    public void cancelarEdicion() {
        this.vehiculoSelected = vehiculoService.findFullById(this.vehiculoSelected.getIdVehiculo());
        this.readOnlyEditForm = true;
        PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo");
    }

    public void guardarEdicion() {
        try {
            this.vehiculoSelected.setModificadoPor(userSessionBean.getUserName());
            vehiculoService.editar(this.vehiculoSelected);
            this.vehiculoSelected = vehiculoService.findFullById(this.vehiculoSelected.getIdVehiculo());
            this.readOnlyEditForm = true;
            Messages.addInfo("Se ha actualizado correctamente la información del vehículo");
            PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo", "growl");
        } catch (Exception e) {
            log.error("Error al guardar la información vehiculo", e);
            Messages.addError(e.getMessage());
            PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo", "growl");
        }
    }

    public void abrirModalRegistroMantenimiento(Long idVehiculo) {
        log.info("abrir modal registro mantenimiento");
        this.vehiculoSelected = vehiculoService.findById(idVehiculo);
    }

    public void abrirModalAdjuntarFotos(Long idVehiculo) {
        log.info("abrir modal adjuntar fotos");
        this.showAdjuntarFotos = true;
        this.vehiculoFotoSelected = vehiculoService.findById(idVehiculo);
        PrimeFaces.current().ajax().update("form_adjuntar_fotos", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').show();");
    }

    public void cerrarModalAdjuntarFotos() {
        log.info("cerrar modal adjuntar fotos");
        this.showAdjuntarFotos = false;
        this.vehiculoFotoSelected = null;
        PrimeFaces.current().ajax().update("form_adjuntar_fotos");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').hide();");
    }

    public void subirFotoVehiculo(FileUploadEvent event) {
        log.info("subir foto vehiculo");
        String fileName = event.getFile().getFileName();
        try {
            if(this.vehiculoFotoSelected != null) {
                String filePath = SaveFile.importFileToPath(event.getFile().getContent(), fileName, FOLDER_VEHICULOS);

                VehiculoFoto vehiculoFoto = VehiculoFoto.builder()
                        .vehiculo(this.vehiculoFotoSelected)
                        .rutaArchivo(filePath)
                        .nombreArchivo(fileName)
                        .fechaCreacion(new Date())
                        .creadoPor(userSessionBean.getUserName())
                        .borrado(0)
                        .build();

                vehiculoFotoService.guardarFoto(vehiculoFoto);
                Messages.addInfo("Se ha gurdado correctamente la foto: " + fileName);
            } else {
                Messages.addWarn("No se ha seleccionado el vehículo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }
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

    private List<Long> getIdVehiculoSelectedList() {
        if(this.vehiculoSelectedList.isEmpty())
            throw new BadRequestException("No ha seleccionado ningún vehículo.");

        return vehiculoSelectedList.stream()
                .map(Vehiculo::getIdVehiculo)
                .toList();
    }

    //endregion private methods

}
