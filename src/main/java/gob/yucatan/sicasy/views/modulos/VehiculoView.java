package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.annotations.ConfigPermisoArray;
import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.excel.models.*;
import gob.yucatan.sicasy.utils.export.excel.services.iface.IGeneratorExcelFile;
import gob.yucatan.sicasy.utils.imports.excel.ConfigHeaderExcelModel;
import gob.yucatan.sicasy.utils.imports.excel.ImportExcelFile;
import gob.yucatan.sicasy.utils.imports.excel.SaveFile;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.ResponsiveOption;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "VEHICULOS_VIEW",
        nombre = "Módulo de Vehículos", descripcion = "Permite ver y filtrar la información de los vehículos.",
        url = "/views/modulos/vehiculos.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_VIEW')")
public class VehiculoView implements Serializable {

    // Constantes
    @Value("${app.files.folder.layouts.importar-vehiculo}")
    private @Getter String LAYOUT_VEHICULOS;
    @Value("${app.files.folder.vehiculos}")
    private @Getter String FOLDER_VEHICULOS;
    private @Getter final Integer ACCION_RECHAZAR_SOLICITUD = 3;
    private @Getter final Integer ACCION_CANCELAR_SOLICITUD = 4;
    private @Getter final Integer ACCION_SOLICITAR_BAJA = 5;
    private @Getter final Integer ACCION_SOLICITAR_MODIFICACION = 6;

    // Inyección de dependencias
    private final UserSessionBean userSessionBean;
    private final IVehiculoService vehiculoService;
    private final IDependenciaService dependenciaService;
    private final ICondicionVehiculoService condicionVehiculoService;
    private final IEstatusVehiculoService estatusVehiculoService;
    private final ILicitacionService licitacionService;
    private final IAnexoService anexoService;
    private final IVehiculoFotoService vehiculoFotoService;
    private final IGeneratorExcelFile generatorExcelFile;
    private final ITipoMantenimientoService tipoMantenimientoService;
    private final IMantenimientoService mantenimientoService;
    private final IMantenimientoFotoService mantenimientoFotoService;
    private final IBitacoraVehiculoService bitacoraVehiculoService;

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter List<Vehiculo> vehiculoList;
    private @Getter @Setter List<Vehiculo> vehiculoSelectedList;
    private @Getter @Setter List<Vehiculo> vehiculoImportList;
    private @Getter @Setter Vehiculo vehiculoSelected;
    private @Getter @Setter Mantenimiento mantenimientoVehiculo;
    private @Getter List<TipoMantenimiento> tipoMantenimientoList;
    private @Getter @Setter Vehiculo vehiculoFilter;
    private @Getter boolean showVehiculosPanel; // Seccion completa de filtrado
    private @Getter @Setter List<VehiculoFoto> vehiculoFotoList;
    private @Getter @Setter List<MantenimientoFoto> mantenimientoFotoList;
    private @Getter List<ResponsiveOption> responsiveOptions;
    private @Getter List<Integer> idEstatusVehiculoList;
    private @Getter List<ResponsiveOption> responsiveOptionsGallery;
    private @Getter int activeIndex = 0;

    // Registro nuevos
    private @Getter boolean showNuevoFormDialog;
    private @Getter boolean showErrorImportacion;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;

    // Seccion completa de detalles del vehículo
    private @Getter boolean showDetailsPanel;
    private @Getter boolean readOnlyEditForm;

    // mantenimiento vehiculos
    private @Getter boolean fechaFinalValida;
    private @Getter List<Mantenimiento> mantenimientoVehiculoList;
    private @Getter List<BitacoraVehiculo> bitacoraVehiculoList;
    private @Getter @Setter UploadedFile fotoMantenimiento;

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


    @PostConstruct
    public void init() {
        log.info("PostConstruct - VehiculoView");
        this.title = "Vehículo";
        this.loadResponsiveOptionsGallery();
        this.getPermisosFiltroEstatus();
        this.limpiarFiltros();

        this.responsiveOptions = new ArrayList<>();
        this.responsiveOptions.add(new ResponsiveOption("1024px", 5));
        this.responsiveOptions.add(new ResponsiveOption("768px", 3));
        this.responsiveOptions.add(new ResponsiveOption("560px", 1));
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros - VehiculoView");
        this.vehiculoSelected = null;
        this.mantenimientoVehiculo = new Mantenimiento();
        this.mantenimientoVehiculo.setTipoMantenimiento(new TipoMantenimiento());
        this.vehiculoFilter = new Vehiculo();
        this.vehiculoFilter.setDependencia(new Dependencia());
        this.vehiculoFilter.setCondicionVehiculo(new CondicionVehiculo());
        this.vehiculoFilter.setEstatusVehiculo(new EstatusVehiculo());
        this.vehiculoFilter.setLicitacion(new Licitacion());
        this.vehiculoFilter.setAnexo(new Anexo());
        this.vehiculoFilter.setIdEstatusVehiculoList(this.idEstatusVehiculoList);

        this.loadDependencias();
        this.loadCondicionVehiculos();
        this.loadEstatusVehiculos();
        this.loadLicitaciones();
        this.loadAnexos();
        this.loadMarcas();
        this.loadModelos();
        this.loadAnios();
        this.loadTipoMantenimiento();

        this.showVehiculosPanel = true;
        this.showDetailsPanel = false;
        this.showNuevoFormDialog = false;

        this.showConfirmEstatus = false;
        this.showAdjuntarFotos = false;

        this.vehiculoList = new ArrayList<>();
        this.vehiculoSelectedList = new ArrayList<>();
        this.vehiculoFotoList = new ArrayList<>();
        this.mantenimientoFotoList = new ArrayList<>();
        this.mantenimientoVehiculoList = new ArrayList<>();
        this.bitacoraVehiculoList = new ArrayList<>();

        this.buscar();
        PrimeFaces.current().ajax().update("form_filtros", "form_datatable");
    }

    public void buscar() {
        log.info("buscar - VehiculoView");
        this.vehiculoList = vehiculoService.findAllDynamic(this.vehiculoFilter);
        List<Vehiculo> vehiculos = vehiculoService.findAllDynamic(this.vehiculoFilter);
        if(!vehiculos.isEmpty()) {
            this.vehiculoList = vehiculos.stream()
                    .sorted(Comparator
                            .comparing((Vehiculo v) -> v.getFechaModificacion() != null ? v.getFechaModificacion() : v.getFechaCreacion(), Comparator.reverseOrder())
                    )
                    .toList();
        }
        else {
            this.vehiculoList = new ArrayList<>();
        }
        PrimeFaces.current().ajax().update("form_datatable");
        PrimeFaces.current().executeScript("PF('dt_vehiculos').unselectAllRows()");
    }

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_REGISTRO_INDIVIDUAL", orden = 1,
                    nombre = "Registro individual", descripcion = "Permite registrar un nuevo vehículo."),
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_REGISTRO_LAYOUT", orden = 2,
                    nombre = "Importar layout", descripcion = "Permite registrar vehículos por medio de la importación de un layout."),
    })
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_REGISTRO_INDIVIDUAL', 'VEHICULOS_WRITE_REGISTRO_LAYOUT')")
    public void abrirModalRegistroVehiculo() {
        log.info("abrirModalRegistroVehiculo - VehiculoView");
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
        log.info("cerrarModalRegistroVehiculo - VehiculoView");
        this.showNuevoFormDialog = false;
        this.vehiculoSelected = null;
        PrimeFaces.current().ajax().update("registrar-vehiculo-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarVehiculoDialog').hide()");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_REGISTRO_INDIVIDUAL')")
    public void guardarNuevoVehiculo() {
        log.info("guardarNuevoVehiculo - VehiculoView");
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

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_REGISTRO_LAYOUT')")
    public void guardarImportacionVehiculo() {
        log.info("guardarImportacionVehiculo - VehiculoView");
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

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_OBTENER_LAYOUT", orden = 7,
            nombre = "Obtener layout de importación", descripcion = "Permite descargar el layout de importación de vehículos.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_REGISTRO_LAYOUT')")
    public void importarLayout(FileUploadEvent event) {
        try {
            log.info("importarLayout - VehiculoView");
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
                list.add(ConfigHeaderExcelModel.builder().header("DIRECTOR_ADMINISTRATIVO").fieldName("autorizaDirectorAdmin").columnIndex(17).build());
                list.add(ConfigHeaderExcelModel.builder().header("DIRECTOR_GENERAL").fieldName("autorizaDirectorGeneral").columnIndex(18).build());

                ImportExcelFile<Vehiculo> importExcelFile = new ImportExcelFile<>();
                this.vehiculoImportList = importExcelFile.processExcelFile(fileContent, vehiculoClass, list);

                this.layoutFileUpload = fileName;
                PrimeFaces.current().ajax().update("tab-view:layout-form:dropZoneLayout");
                log.info("Se ha cargado la información del layout correctamente. Vehículos a importar: {}", this.vehiculoImportList.size());
            } else {
                // Manejar otros tipos de archivos si es necesario
                // Por ejemplo, mostrar un mensaje de error
                Messages.addError("Error", "Tipo de archivo no válido");
                PrimeFaces.current().executeScript("PF('uploadLayout').clear();");
            }
        } catch (Exception e) {
            if(e instanceof NotFoundException) {
                log.warn(e.getMessage());
                Messages.addWarn(e.getMessage());
            } else if(e instanceof BadRequestException) {
                log.warn(e.getMessage());
                Messages.addError(e.getMessage());
            } else {
                log.error(e.getMessage(), e);
                Messages.addError("Ocurrió un error al procesar la información del archivo. Consulte el log.");
            }
            PrimeFaces.current().executeScript("PF('uploadLayout').clear();");
        }
    }


    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_SOLICITAR_AUTORIZACION", orden = 3,
            nombre = "Solicitar autorización", descripcion = "Permite cambiar de estado REGISTRADO a POR AUTORIZAR los vehículos seleccionados.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_SOLICITAR_AUTORIZACION')")
    public void solicitarAutorizacion() {
        log.info("solicitarAutorizacion - VehiculoView");
        try {
            List<Long> idVehiculoSelectedList = this.getIdVehiculoSelectedList();
            vehiculoService.solicitarAutorizacion(idVehiculoSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se ha enviado la solicitud de autorización");
        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_AUTORIZAR_SOLICITUD", orden = 4,
            nombre = "Autorizar solicitud", descripcion = "Permite cambiar de estado POR AUTORIZAR a ACTIVO los vehículos seleccionados.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_AUTORIZAR_SOLICITUD')")
    public void autorizarSolicitud() {
        log.info("autorizarSolicitud - VehiculoView");
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

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_RECHAZAR_SOLICITUD", orden = 5,
                    nombre = "Rechazar solicitud", descripcion = "Permite cambiar de estado POR AUTORIZAR a REGISTRADO los vehículos seleccionados."),
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_CANCELAR_SOLICITUD", orden = 6,
                    nombre = "Cancelar solicitud", descripcion = "Permite cambiar de estado POR AUTORIZAR a CANCELADO los vehículos seleccionados."),
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_SOLICITAR_BAJA", orden = 7,
                    nombre = "Solicitar baja", descripcion = "Permite cambiar de estado ACTIVO a BAJA los vehículos seleccionados."),
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_SOLICITAR_MODIFICACION", orden = 8,
                    nombre = "Solicitar modificación", descripcion = "Permite cambiar de estado ACTIVO o BAJA a REGISTRADO los vehículos seleccionados.")
    })
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_RECHAZAR_SOLICITUD', 'VEHICULOS_WRITE_CANCELAR_SOLICITUD', 'VEHICULOS_WRITE_SOLICITAR_BAJA', 'VEHICULOS_WRITE_SOLICITAR_MODIFICACION')")
    public void abrirConfirmEstatusDialog(Integer confirmAccion) {
        log.info("abrirConfirmEstatusDialog - VehiculoView");
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
            this.confirmMensaje = "¿Está seguro que desea solicitar la modificación de los vehículos?";
        }

        PrimeFaces.current().ajax().update("confirm-estatus-dialog");
        PrimeFaces.current().executeScript("PF('confirmEstatusDialog').show()");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_RECHAZAR_SOLICITUD', 'VEHICULOS_WRITE_CANCELAR_SOLICITUD', 'VEHICULOS_WRITE_SOLICITAR_BAJA', 'VEHICULOS_WRITE_SOLICITAR_MODIFICACION')")
    public void confirmarEstatusDialog() {
        log.info("confirmarEstatusDialog - VehiculoView");
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

            if(success) {
                this.buscar();
                PrimeFaces.current().executeScript("PF('confirmEstatusDialog').hide()");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void cerrarConfirmEstatusDialog() {
        log.info("cerrarConfirmEstatusDialog - VehiculoView");
        this.showConfirmEstatus = false;
        this.confirmAccion = null;
        this.confirmMotivo = "";
        this.confirmMensaje = "";
        PrimeFaces.current().ajax().update("confirm-estatus-dialog");
        PrimeFaces.current().executeScript("PF('confirmEstatusDialog').hide()");
    }


    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_VER_DETALLE", orden = 9,
            nombre = "Ver detalle del vehículo", descripcion = "Permite ver el detalle la información del vehículo, se accede al panel de edición y bitácoras.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_VER_DETALLE')")
    public void verDetalle(Vehiculo vehiculo) {
        log.info("verDetalle - VehiculoView");
        this.vehiculoSelected = vehiculoService.findFullById(vehiculo.getIdVehiculo());
        this.showVehiculosPanel = false;
        this.showDetailsPanel = true;
        this.readOnlyEditForm = true;

        this.loadLicitacionesForm();
        this.loadAnexosForm();

        this.vehiculoFotoList = vehiculoFotoService.getVehiculoFotos(this.vehiculoSelected.getIdVehiculo());
        this.mantenimientoVehiculoList = mantenimientoService.findByVehiculoId(vehiculo.getIdVehiculo());
        this.bitacoraVehiculoList = bitacoraVehiculoService.findByVehiculoId(vehiculo.getIdVehiculo());

        PrimeFaces.current().ajax().update("container");
    }

    public void regresar() {
        log.info("regresar - VehiculoView");
        this.vehiculoSelected = null;
        this.showVehiculosPanel = true;
        this.showDetailsPanel = false;
        PrimeFaces.current().ajax().update("container");
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_EDITAR_VEHICULO", orden = 10,
            nombre = "Editar vehículo", descripcion = "Permite editar la información del vehículo.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_EDITAR_VEHICULO')")
    public void permitirEditar() {
        log.info("permitirEditar - VehiculoView");
        this.readOnlyEditForm = false;
        PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo");
    }

    public void cancelarEdicion() {
        this.vehiculoSelected = vehiculoService.findFullById(this.vehiculoSelected.getIdVehiculo());
        this.readOnlyEditForm = true;
        PrimeFaces.current().ajax().update("tab_view_detalles:form_editar_vehiculo");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_EDITAR_VEHICULO')")
    public void guardarEdicion() {
        log.info("guardarEdicion - VehiculoView");
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_REGISTRAR_MANTENIMIENTO", orden = 13,
            nombre = "Adjuntar fotos", descripcion = "Permite adjuntar fotos del vehículo.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_REGISTRAR_MANTENIMIENTO')")
    public void abrirModalRegistroMantenimiento(Long idVehiculo) {
        log.info("abrirModalRegistroMantenimiento - VehiculoView");
        this.vehiculoSelected = vehiculoService.findById(idVehiculo);
        this.mantenimientoVehiculo = new Mantenimiento();
        this.mantenimientoVehiculo.setVehiculo(vehiculoSelected);
        this.mantenimientoVehiculo.setTipoMantenimiento(new TipoMantenimiento());
        PrimeFaces.current().ajax().update("form_registrar_mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('registroMantenimientoDialog').show();");
    }

    public void cerrarModalRegistroMantenimiento(){
        log.info("cerrarModalRegistroMantenimiento - VehiculoView");
        this.vehiculoSelected = null;
        PrimeFaces.current().ajax().update("form_registrar_mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('registroMantenimientoDialog').hide();");

    }

    public void abrirModalFotosMantenimiento2(Mantenimiento mantenimiento){
        log.info("abrirModalFotosMantenimiento2 - VehiculoView");
        this.mantenimientoVehiculo = mantenimiento;

        PrimeFaces.current().ajax().update("form_adjuntar_fotos-mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarFotosMantenimientoDialog').show();");
    }

    public void abrirModalAdjuntaVerFotosMantenimiento(Mantenimiento mantenimiento){
        log.info("abrirModalAdjuntaVerFotosMantenimiento - VehiculoView");
        this.mantenimientoVehiculo = mantenimiento;

        this.mantenimientoFotoList = mantenimientoFotoService.getFotosMantenimientos(mantenimientoVehiculo.getIdMantenimiento());

        log.info("abrir modal adjuntaVerFotosMantenimiento");
        PrimeFaces.current().ajax().update("form_galeria_mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('galeriaFotosMantenimientoDialog').show();");
    }

    public void cerrarGaleriaFotosMantenimiento() { //
        log.info("cerrarGaleriaFotosMantenimiento - VehiculoView");
        this.vehiculoSelected = null;
        PrimeFaces.current().ajax().update("form_adjuntar_fotos-mantenimiento form_galeria_mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarFotosMantenimientoDialog').hide();");
    }

    public void abrirModalEditarMantenimiento(Mantenimiento mantenimiento) {
        log.info("abrirModalEditarMantenimiento - VehiculoView");
        this.mantenimientoVehiculo = mantenimiento;
        PrimeFaces.current().ajax().update("form_registrar_mantenimiento", "growl");
        PrimeFaces.current().executeScript("PF('registroMantenimientoDialog').show();");
    }

    public void doEliminarMatenimiento(Mantenimiento mantenimiento) {
        log.info("doEliminarMatenimiento - VehiculoView");
        this.mantenimientoVehiculo = mantenimiento;
    }

    public void eliminarMantenimiento(){
        log.info("eliminarMantenimiento - VehiculoView");
        this.mantenimientoVehiculo.setBorradoPor(userSessionBean.getUserName());
        this.mantenimientoVehiculo.setFechaBorrado(new Date());
        this.mantenimientoVehiculo.setEstatus(EstatusRegistro.BORRADO);

        try{
            mantenimientoService.save(mantenimientoVehiculo);
            this.mantenimientoVehiculoList = mantenimientoService.findByVehiculoId(mantenimientoVehiculo.getVehiculo().getIdVehiculo());

            Messages.addInfo("Atención","Se ha eliminado la información");
            PrimeFaces.current().ajax().update("tab_view_detalles:frm_mantenimientos:dt_mantenimiento", "growl");
            PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

        }catch (Exception e){
            Messages.addError(e.getMessage());
        }

    }

    public void guardarMantenimientoVehiculo(){
        log.info("guardarMantenimientoVehiculo - VehiculoView");
        try{
            if(this.mantenimientoVehiculo.getTipoMantenimiento().getIdTipoMantenimiento() != null &&
                this.mantenimientoVehiculo.getDescripcion() != null && !this.mantenimientoVehiculo.getDescripcion()
                .isEmpty()) {

                if (this.mantenimientoVehiculo.getIdMantenimiento() != null){
                    // update data
                    this.mantenimientoVehiculo.setModificadoPor(userSessionBean.getUserName());
                    this.mantenimientoVehiculo.setFechaModificacion(new Date());

                }else {
                    // registro nuevo
                    this.mantenimientoVehiculo.setCreadoPor(userSessionBean.getUserName());
                    this.mantenimientoVehiculo.setFechaCreacion(new Date());
                    this.mantenimientoVehiculo.setEstatus(EstatusRegistro.ACTIVO);

                }

                this.mantenimientoVehiculo = mantenimientoService.save(mantenimientoVehiculo);

                if (fotoMantenimiento != null){
                    subirFotoMantenimiento();
                }

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operación exitosa", "Se ha guardado correctamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("tab_view_detalles:frm_mantenimientos:dt_mantenimiento", "growl");
                PrimeFaces.current().executeScript("PF('registroMantenimientoDialog').hide();");
                this.buscar();
                this.mantenimientoVehiculo  = new Mantenimiento();
                this.mantenimientoVehiculo.setTipoMantenimiento(new TipoMantenimiento());
            }else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Atención", "Seleccione un tipo de mantenimiento y escriba una descripción para guardar el registro.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }


        }catch (Exception ex){
            String message;
            if(ex instanceof BadRequestException)
                message = ex.getMessage();
            else if(ex instanceof NotFoundException)
                message = ex.getMessage();
            else
                message = "Ocurrió un error inesperado.";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", message);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    public void validateFechaInicioFinal(SelectEvent event){
        log.info("validateFechaInicioFinal - VehiculoView");
        if (this.mantenimientoVehiculo.getFechaInicio() != null && this.mantenimientoVehiculo.getFechaFin() != null){

            if (this.mantenimientoVehiculo.getFechaInicio().after(this.mantenimientoVehiculo.getFechaFin())){
                // la fecha de inicio no puede estar dspues de la fecha final
                this.fechaFinalValida = false;
                FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Fecha selecionada inválida."));
            }else
                fechaFinalValida = true;

        }else
            fechaFinalValida = true;

        PrimeFaces.current().ajax().update("form_registrar_mantenimiento:bnt_saveRegistro");

    }



    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_ADJUNTAR_FOTOS", orden = 11,
            nombre = "Adjuntar fotos", descripcion = "Permite adjuntar fotos del vehículo.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_ADJUNTAR_FOTOS')")
    public void abrirModalAdjuntarFotos(Long idVehiculo) {
        log.info("abrirModalAdjuntarFotos - VehiculoView");
        this.showAdjuntarFotos = true;
        this.vehiculoFotoSelected = vehiculoService.findById(idVehiculo);
        PrimeFaces.current().ajax().update("form_adjuntar_fotos", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').show();");
    }

    public void cerrarModalAdjuntarFotos() {
        log.info("cerrarModalAdjuntarFotos - VehiculoView");
        this.showAdjuntarFotos = false;
        this.vehiculoFotoSelected = null;
        PrimeFaces.current().ajax().update("form_adjuntar_fotos");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').hide();");
    }

    public void subirFotoMantenimiento2(FileUploadEvent event) {
        log.info("subirFotoMantenimiento2 - VehiculoView");
        String fileName = event.getFile().getFileName();
        try {
            if(this.mantenimientoVehiculo != null) {
                String filePath = SaveFile.importFileToPath(event.getFile().getContent(), fileName, FOLDER_VEHICULOS);

                MantenimientoFoto mantenimientoFoto = MantenimientoFoto.builder()
                        .mantenimiento(mantenimientoVehiculo)
                        .rutaArchivo(filePath)
                        .nombreArchivo(fileName)
                        .fechaCreacion(new Date())
                        .creadoPor(userSessionBean.getUserName())
                        .borrado(0)
                        .build();

                mantenimientoFotoService.guardarFoto(mantenimientoFoto);
                Messages.addInfo("Se ha guardado correctamente la foto: " + fileName);

                this.mantenimientoFotoList = mantenimientoFotoService.getFotosMantenimientos(mantenimientoFoto.getMantenimiento().getIdMantenimiento());
                PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria_mantenimiento"); // aqui actualiza la galeria correspondiente

            } else {
                Messages.addWarn("No se ha seleccionado el mantenimiento del vehiculo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }
    }

    public void subirFotoMantenimiento(){
        log.info("subirFotoMantenimiento - VehiculoView");
        String fileName; // event.getFile().getFileName();
        fileName = fotoMantenimiento.getFileName();
        try {
            if(this.mantenimientoVehiculo != null) {
                String filePath = SaveFile.importFileToPath(fotoMantenimiento.getContent(), fileName, FOLDER_VEHICULOS);

                MantenimientoFoto mantenimientoFoto = MantenimientoFoto.builder()
                        .mantenimiento(mantenimientoVehiculo)
                        .rutaArchivo(filePath)
                        .nombreArchivo(fileName)
                        .fechaCreacion(new Date())
                        .creadoPor(userSessionBean.getUserName())
                        .borrado(0)
                        .build();


                mantenimientoFotoService.guardarFoto(mantenimientoFoto);
                Messages.addInfo("Se ha guardado correctamente la foto: " + fileName);

            } else {
                Messages.addWarn("No se ha seleccionado el mantenimiento del vehiculo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }

    }

    public void subirFotoVehiculo(FileUploadEvent event) {
        log.info("subirFotoVehiculo - VehiculoView");
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
                Messages.addInfo("Se ha guardado correctamente la foto: " + fileName);

                if(this.showDetailsPanel) {
                    this.vehiculoFotoList = vehiculoFotoService.getVehiculoFotos(this.vehiculoFotoSelected.getIdVehiculo());
                    PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria");
                }
            } else {
                Messages.addWarn("No se ha seleccionado el vehículo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "VEHICULOS_WRITE_BORRAR_FOTOS", orden = 12,
            nombre = "Borrar foto", descripcion = "Permite borrar fotos del vehículo.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_WRITE_BORRAR_FOTOS')")
    public void borrarFoto(Long vehiculoFotoId) {
        log.info("borrarFoto - VehiculoView");
        try {
            vehiculoFotoService.borrarFoto(vehiculoFotoId, userSessionBean.getUserName());
            this.vehiculoFotoList = vehiculoFotoService.getVehiculoFotos(this.vehiculoFotoSelected.getIdVehiculo());
            PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria");

        } catch (Exception e) {
            Messages.addError(e.getMessage());
        }
    }

    public void borrarFotoMantenimiento(Long idMantenimientoFoto){
        log.info("borrarFotoMantenimiento - VehiculoView");
        try {
            mantenimientoFotoService.borrarFoto(idMantenimientoFoto, userSessionBean.getUserName());
            this.mantenimientoFotoList = mantenimientoFotoService.getFotosMantenimientos(this.mantenimientoVehiculo.getIdMantenimiento());
            PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria_mantenimiento");
        }catch (Exception e) {
            Messages.addError(e.getMessage());
        }

    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_EXPORTAR_VEHICULOS", orden = 6,
            nombre = "Exportar", descripcion = "Permite exportar en Excel el listado de vehículos.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'VEHICULOS_READ_EXPORTAR_VEHICULOS')")
    public ExportFile exportarVehiculos() throws IOException {
        log.info("exportarVehiculos - VehiculoView");
        if(this.vehiculoList != null && !this.vehiculoList.isEmpty()) {

            List<ExcelCell> cellList = new ArrayList<>();

            XSSFWorkbook workbook = generatorExcelFile.createWorkbook();

            XSSFCellStyle centerTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                    .workbook(workbook)
                    .fontSize(12)
                    .fontColor(ExcelFontColor.BLACK)
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.TOP)
                    .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                    .dataFormat("0")
                    .build());

            XSSFCellStyle centerFloatTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                    .workbook(workbook)
                    .fontSize(12)
                    .fontColor(ExcelFontColor.BLACK)
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.TOP)
                    .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                    .dataFormat("0.00")
                    .build());

            XSSFCellStyle leftTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                    .workbook(workbook)
                    .fontSize(12)
                    .fontColor(ExcelFontColor.BLACK)
                    .horizontalAlignment(HorizontalAlignment.LEFT)
                    .verticalAlignment(VerticalAlignment.TOP)
                    .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                    .isWrapText(true)
                    .build());

            cellList.add(ExcelCell.builder().columnName("ID").propertyExpression("idVehiculo").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. SERIE").propertyExpression("noSerie").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("PLACA").propertyExpression("placa").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("AÑO").propertyExpression("anio").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MARCA").propertyExpression("marca").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MODELO").propertyExpression("modelo").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("COLOR").propertyExpression("color").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("CODICIÓN").propertyExpression("condicionVehiculo != null ? condicionVehiculo.nombre : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("ESTATUS").propertyExpression("estatusVehiculo != null ? estatusVehiculo.nombre : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DEPENDENCIA").propertyExpression("dependencia != null ? dependencia.abreviatura + \" - \" + dependencia.nombre : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DEPENDENCIA ASIGNADA").propertyExpression("dependenciaAsignada != null ? dependenciaAsignada.abreviatura + \" - \" + dependenciaAsignada.nombre : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("LICITACIÓN").propertyExpression("licitacion != null ? licitacion.numeroLicitacion : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("ANEXO").propertyExpression("anexo != null ? anexo.nombre : \"\"").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. MOTOR").propertyExpression("noMotor").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("RENTA MENSUAL").propertyExpression("rentaMensual").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. FACTURA").propertyExpression("noFactura").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("VALOR FACTURA").propertyExpression("montoFactura").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DESCRIPCIÓN").propertyExpression("descripcionVehiculo").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("RESGUARDANTE").propertyExpression("resguardante").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("ÁREA RESGUARDANTE").propertyExpression("areaResguardante").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DIRECTOR ADMINISTRATIVO").propertyExpression("autorizaDirectorAdmin").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DIRECTOR GENERAL").propertyExpression("autorizaDirectorGeneral").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("PROVEEDOR").propertyExpression("proveedor").cellStyle(leftTopStyle).cellAutoSize(true).build());

            ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                    .data(this.vehiculoList)
                    .cells(cellList)
                    .sheetName("VEHICULOS")
                    .filename("vehiculos")
                    .autoFilter(true)
                    .agregarFechaGeneracion(true)
                    .appName("SICASY")
                    .build();

            return generatorExcelFile.createExcelFile(workbook, excelDataSheet);
        }

        return new ExportFile();
    }

    public void changeActiveIndex() {
        log.info("changeActiveIndex - SiniestrosView");
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        this.activeIndex = Integer.parseInt(params.get("index"));
        log.info("activeIndex {}", this.activeIndex);
    }

    public void presentGallery() {
        log.info("presentGallery - SiniestrosView");
        this.activeIndex = 0;
    }

    //region Events

    public void onChangeLicitacionFilter() {
        this.loadAnexos();
    }

    public void onChangeMarcaFilter() {
        this.loadModelos();
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
        this.estatusVehiculoList = estatusVehiculoService.findAllDropdown(this.idEstatusVehiculoList);
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

    private void loadAnios() {
        this.anioList = vehiculoService.findDistinctAnio();
    }

    private void loadTipoMantenimiento(){
        this.tipoMantenimientoList = tipoMantenimientoService.findAll();
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

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_FILTRAR_ESTATUS_REGISTRADO", orden = 1,
                    nombre = "Filtrar Estatus REGISTRADO", descripcion = "Permite filtrar los vehículos con estatus REGISTRADO."),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_FILTRAR_ESTATUS_POR_AUTORIZAR", orden = 2,
                    nombre = "Filtrar Estatus POR AUTORIZAR", descripcion = "Permite filtrar los vehículos con estatus POR AUTORIZAR."),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_FILTRAR_ESTATUS_ACTIVO", orden = 3,
                    nombre = "Filtrar Estatus ACTIVO", descripcion = "Permite filtrar los vehículos con estatus ACTIVO."),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_FILTRAR_ESTATUS_BAJA", orden = 4,
                    nombre = "Filtrar Estatus BAJA", descripcion = "Permite filtrar los vehículos con estatus BAJA."),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "VEHICULOS_READ_FILTRAR_ESTATUS_CANCELADO", orden = 5,
                    nombre = "Filtrar Estatus CANCELADO", descripcion = "Permite filtrar los vehículos con estatus CANCELADO."),
    })
    private void getPermisosFiltroEstatus() {
        Integer ESTATUS_REGISTRADO = 1;
        Integer ESTATUS_POR_AUTORIZAR = 2;
        Integer ESTATUS_ACTIVO = 3;
        Integer ESTATUS_BAJA = 4;
        Integer ESTATUS_CANCELADO = 5;

        this.idEstatusVehiculoList = new ArrayList<>();

        if(userSessionBean.userHasAuthority("VEHICULOS_READ_FILTRAR_ESTATUS_REGISTRADO") ||
                userSessionBean.userHasAuthority("ROLE_OWNER"))
            this.idEstatusVehiculoList.add(ESTATUS_REGISTRADO);

        if(userSessionBean.userHasAuthority("VEHICULOS_READ_FILTRAR_ESTATUS_POR_AUTORIZAR") ||
                userSessionBean.userHasAuthority("ROLE_OWNER"))
            this.idEstatusVehiculoList.add(ESTATUS_POR_AUTORIZAR);

        if(userSessionBean.userHasAuthority("VEHICULOS_READ_FILTRAR_ESTATUS_ACTIVO") ||
                userSessionBean.userHasAuthority("ROLE_OWNER"))
            this.idEstatusVehiculoList.add(ESTATUS_ACTIVO);

        if(userSessionBean.userHasAuthority("VEHICULOS_READ_FILTRAR_ESTATUS_BAJA") ||
                userSessionBean.userHasAuthority("ROLE_OWNER"))
            this.idEstatusVehiculoList.add(ESTATUS_BAJA);

        if(userSessionBean.userHasAuthority("VEHICULOS_READ_FILTRAR_ESTATUS_CANCELADO") ||
                userSessionBean.userHasAuthority("ROLE_OWNER"))
            this.idEstatusVehiculoList.add(ESTATUS_CANCELADO);
    }

    private void loadResponsiveOptionsGallery() {
        this.responsiveOptionsGallery = new ArrayList<>();
        responsiveOptionsGallery.add(new ResponsiveOption("1024px", 5));
        responsiveOptionsGallery.add(new ResponsiveOption("768px", 3));
        responsiveOptionsGallery.add(new ResponsiveOption("560px", 1));
    }

    //endregion private methods

}
