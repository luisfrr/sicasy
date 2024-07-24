package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.annotations.ConfigPermisoArray;
import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.dtos.EndosoBaja;
import gob.yucatan.sicasy.business.dtos.EndosoModificacion;
import gob.yucatan.sicasy.business.dtos.PagoInciso;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IIncisoService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.excel.models.*;
import gob.yucatan.sicasy.utils.export.excel.services.iface.IGeneratorExcelFile;
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
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "POLIZA_VIEW",
        nombre = "Módulo de Pólizas e Incisos",
        descripcion = "Permite ver y filtrar la información de las pólizas e incisos.",
        url = "/views/modulos/polizas.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_VIEW')")
public class PolizaView implements Serializable {

    // Constantes
    @Value("${app.files.folder.layouts.importar-registro-poliza}")
    private @Getter String LAYOUT_REGISTRO_POLIZA;
    @Value("${app.files.folder.layouts.importar-endoso-alta}")
    private @Getter String LAYOUT_ENDOSO_ALTA;
    @Value("${app.files.folder.polizas_facturas}")
    private @Getter String FOLDER_POLIZAS_FACTURAS;
    @Value("${app.files.folder.polizas}")
    private @Getter String FOLDER_POLIZAS;
    private @Getter final Integer ESTATUS_INCISO_REGISTRADA = 1;
    private @Getter final Integer ESTATUS_INCISO_EN_PROCESO_PAGO = 2;
    private @Getter final Integer ESTATUS_INCISO_PAGADA = 3;
    private @Getter final Integer ESTATUS_INCISO_BAJA = 4;

    // Inyección de dependencias
    private final UserSessionBean userSessionBean;
    private final IPolizaService polizaService;
    private final IAseguradoraService aseguradoraService;
    private final IIncisoService incisoService;
    private final IVehiculoService vehiculoService;
    private final IGeneratorExcelFile generatorExcelFile;

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter Poliza polizaFilter;
    private @Getter @Setter Poliza polizaForm;
    private @Getter @Setter Poliza polizaSelected;
    private @Getter List<Poliza> polizaList;
    private @Getter List<Inciso> incisoList;
    private @Getter @Setter List<Inciso> incisoSelectedList;
    private @Getter @Setter Inciso incisoSelected;
    private @Getter @Setter Inciso incisoForm;
    private @Getter @Setter String motivoRechazoSolicitud;
    private @Getter @Setter PagoInciso pagoInciso;
    private @Getter @Setter EndosoModificacion endosoModificacion;
    private @Getter @Setter EndosoBaja endosoBaja;
    private @Getter List<Aseguradora> aseguradoraList;
    private @Getter List<Poliza> polizaFormList;
    private @Getter String informacionVehiculo;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;
    private @Getter @Setter String layoutFileUpload;
    private @Getter @Setter List<Poliza> importPolizaList;
    private @Getter @Setter List<Inciso> importIncisoList;
    private @Getter @Setter UploadedFile file;

    // Variables para renderizar
    private @Getter boolean showPanelPolizas;
    private @Getter boolean showRegistrarPolizasDialog;
    private @Getter boolean showAdjuntarPolizaDialog;
    private @Getter boolean showRegistrarEndosoAltaDialog;
    private @Getter boolean showRechazarSolicitudDialog;
    private @Getter boolean showEditarIncisoDialog;
    private @Getter boolean showRegistrarPagoDialog;
    private @Getter boolean showEndosoModificacionDialog;
    private @Getter boolean showEndosoBajaDialog;
    private @Getter boolean showErrorImportacion;
    private Long idPoliza;


    @PostConstruct
    public void init() {
        log.info("PostConstruct - PolizaView");
        this.title = "Pólizas";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros - PolizaView");
        this.polizaFilter = new Poliza();
        this.polizaFilter.setAseguradora(new Aseguradora());

        this.showPanelPolizas = true;
        this.showRegistrarPolizasDialog = false;
        this.showAdjuntarPolizaDialog = false;
        this.showRechazarSolicitudDialog = false;
        this.showEditarIncisoDialog = false;
        this.showRegistrarPagoDialog = false;
        this.showEndosoModificacionDialog = false;
        this.showEndosoBajaDialog = false;

        this.loadAseguradorasList();

        this.polizaList = new ArrayList<>();
        this.incisoList = new ArrayList<>();
        this.polizaSelected = null;
        this.incisoSelectedList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros", "form_datatable");
    }

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_DESCARGAR_POLIZA", orden = 2,
                    nombre = "Descargar póliza", descripcion = "Permite descargar la póliza en formato PDF"),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_EXPORTAR_LAYOUT_REGISTRO_POLIZA", orden = 5,
                    nombre = "Exportar layout registro póliza", descripcion = "Permite exportar el layout de registro de pólizas"),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_EXPORTAR_LAYOUT_ENDOSO_ALTA", orden = 6,
                    nombre = "Exportar layout endoso alta", descripcion = "Permite exportar el layout de endoso de alta"),
    })
    public void buscar() {
        log.info("buscar - PolizaView");
        this.polizaList = polizaService.findAll(this.polizaFilter);
        this.incisoList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_VER_INCISOS", orden = 1,
            nombre = "Ver incisos", descripcion = "Acción que permite visualizar los incisos de una póliza")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_READ_VER_INCISOS')")
    public void verIncisos() {
        log.info("verIncisos - PolizaView");
        if(this.polizaSelected != null) {
            this.polizaSelected = polizaService.findById(this.polizaSelected.getIdPoliza());
            if(this.polizaSelected.getIdPoliza() != null) {
                this.incisoList = incisoService.findByIdPoliza(this.polizaSelected.getIdPoliza());
            } else {
                this.incisoList = new ArrayList<>();
            }
            PrimeFaces.current().ajax().update("form_datatable_incisos");
        }
    }

    public void limpiarIncisos() {
        log.info("limpiarIncisos - PolizaView");
        this.incisoList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_datatable_incisos");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_REGISTRAR_POLIZA', 'POLIZA_WRITE_IMPORTAR_POLIZA')")
    public void abrirRegistroPolizasDialog() {
        log.info("abrirRegistroPolizasDialog - PolizaView");
        this.showRegistrarPolizasDialog = true;
        this.polizaForm = new Poliza();
        this.polizaForm.setAseguradora(new Aseguradora());
        this.acuseImportacionList = new ArrayList<>();
        this.showErrorImportacion = false;
        this.layoutFileUpload = null;
        this.importPolizaList = null;
        PrimeFaces.current().ajax().update("registrar-polizas-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarPolizasDialog').show()");
    }

    public void cerrarRegistroPolizasDialog() {
        log.info("cerrarRegistroPolizasDialog - PolizaView");
        this.showRegistrarPolizasDialog = false;
        this.polizaForm = null;
        this.acuseImportacionList = new ArrayList<>();
        this.showErrorImportacion = false;
        this.layoutFileUpload = null;
        this.importPolizaList = null;
        PrimeFaces.current().ajax().update("registrar-polizas-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarPolizasDialog').hide()");
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_REGISTRAR_POLIZA", orden = 1,
            nombre = "Registrar póliza", descripcion = "Acción que permite registrar una nueva póliza")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_REGISTRAR_POLIZA')")
    public void guardarRegistroPoliza() {
        log.info("guardarRegistroPoliza - PolizaView");
        try {
            if(this.polizaForm != null) {
                this.polizaForm.setCreadoPor(userSessionBean.getUserName());
                polizaService.registrarPoliza(this.polizaForm, userSessionBean.getUserName());
                Messages.addInfo("Se ha guardado correctamente la póliza");
                this.limpiarFiltros();
                this.buscar();
                this.verIncisos();
                this.cerrarRegistroPolizasDialog();
            }
        } catch (Exception e) {
            log.error("Error al importar polizas", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_IMPORTAR_POLIZA", orden = 2,
            nombre = "Importar pólizas", descripcion = "Acción que permite importar nuevos registros de pólizas")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_IMPORTAR_POLIZA')")
    public void importarLayoutRegistroPolizas(FileUploadEvent event) {
        log.info("importarLayoutRegistroPolizas - PolizaView");
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        byte[] fileContent = file.getContent();

        try {
            // Aquí puedes procesar el archivo según su tipo o contenido
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                // Si es un archivo Excel, procesarlo utilizando Apache POI
                Class<Poliza> polizaClass = Poliza.class;
                List<ConfigHeaderExcelModel> list = new ArrayList<>();
                list.add(ConfigHeaderExcelModel.builder().header("ASEGURADORA").fieldName("idAseguradora").columnIndex(0).build());
                list.add(ConfigHeaderExcelModel.builder().header("NO. POLIZA").fieldName("numeroPoliza").columnIndex(1).build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA INICIO").fieldName("fechaInicioVigencia").columnIndex(2).dateFormat("dd/MM/yyyy").build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA FIN").fieldName("fechaFinVigencia").columnIndex(3).dateFormat("dd/MM/yyyy").build());
                list.add(ConfigHeaderExcelModel.builder().header("COBERTURA").fieldName("tipoCobertura").columnIndex(4).build());
                list.add(ConfigHeaderExcelModel.builder().header("BENEFICIARIO").fieldName("beneficiarioPreferente").columnIndex(5).build());

                ImportExcelFile<Poliza> importExcelFile = new ImportExcelFile<>();
                this.importPolizaList = importExcelFile.processExcelFile(fileContent, polizaClass, list);

                this.layoutFileUpload = fileName;
                PrimeFaces.current().ajax().update("tab-view:layout-form:dropZoneLayout");
                log.info("Se ha cargado la información del layout correctamente. Pólizas a importar: {}", this.importPolizaList.size());
            } else {
                // Manejar otros tipos de archivos si es necesario
                // Por ejemplo, mostrar un mensaje de error
                Messages.addError("Error", "Tipo de archivo no válido");
            }
        } catch (Exception e) {
            log.error("Ocurrió un error al importar el layout de registro de pólizas", e);
            Messages.addError("Ocurrió un error al importar el layout de registro de pólizas");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_IMPORTAR_POLIZA')")
    public void guardarLayoutRegistroPolizas() {
        log.info("guardarLayoutRegistroPolizas - PolizaView");
        try {
            if(this.importPolizaList != null) {
                this.acuseImportacionList = polizaService.importarLayoutRegistro(this.importPolizaList,
                        userSessionBean.getUserName());

                // Si alguno marco error entonces no se guardó nada y se muestra el acuse
                if(acuseImportacionList.stream().anyMatch(a -> a.getError() == 1)) {
                    this.showErrorImportacion = true;
                }
                else {
                    // Si no, entonces se guardó correctamente
                    Messages.addInfo("Se han importado las pólizas correctamente.");
                    this.limpiarFiltros();
                    this.buscar();
                    this.verIncisos();
                    this.cerrarRegistroPolizasDialog();
                }
            }
        } catch (Exception e) {
            log.error("Error al importar polizas", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_ELIMINAR", orden = 3,
            nombre = "Eliminar póliza", descripcion = "Acción que permite eliminar el registro de una póliza")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ELIMINAR')")
    public void eliminarPoliza(Long polizaId) {
        log.info("eliminarPoliza - PolizaView");
        try {
            polizaService.borrar(polizaId, userSessionBean.getUserName());

            Messages.addInfo("Se eliminado la póliza correctamente.");
            this.buscar();
            this.verIncisos();

        } catch (Exception e) {
            log.error("Error al eliminar una póliza", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_ADJUNTAR_POLIZA", orden = 4,
            nombre = "Adjuntar póliza", descripcion = "Acción que permite adjuntar el archivo de la póliza en formato PDF")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ADJUNTAR_POLIZA')")
    public void abrirAdjuntarPolizaDialog(Long idPoliza) {
        log.info("abrirAdjuntarPolizaDialog - PolizaView");
        this.showAdjuntarPolizaDialog = true;
        this.polizaForm = polizaService.findById(idPoliza);
        PrimeFaces.current().ajax().update("form_adjuntar_poliza", "form_datatable", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarPolizaDialog').show();");
    }

    public void cerrarAdjuntarPolizaDialog() {
        log.info("cerrarAdjuntarPolizaDialog - PolizaView");
        this.showAdjuntarPolizaDialog = false;
        this.polizaForm = null;
        PrimeFaces.current().ajax().update("form_adjuntar_poliza", "form_datatable");
        PrimeFaces.current().executeScript("PF('adjuntarPolizaDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ADJUNTAR_POLIZA')")
    public void adjuntarPoliza(FileUploadEvent event) {
        log.info("adjuntarPoliza - PolizaView");
        String fileName = event.getFile().getFileName();
        try {
            if(this.polizaForm != null) {
                String filePath = SaveFile.importFileToPath(event.getFile().getContent(), fileName, FOLDER_POLIZAS);

                this.polizaForm.setRutaArchivo(filePath);
                this.polizaForm.setNombreArchivo(fileName);
                this.polizaForm.setFechaModificacion(new Date());
                this.polizaForm.setModificadoPor(userSessionBean.getUserName());

                polizaService.save(this.polizaForm);
                this.buscar();
                this.verIncisos();
                PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos", "growl");
                Messages.addInfo("Se ha guardado correctamente la póliza " + fileName);

            } else {
                Messages.addWarn("No se ha seleccionado la póliza");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la póliza: " + fileName);
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_ALTA', 'POLIZA_WRITE_IMPORTAR_ENDOSO_ALTA')")
    public void abrirRegistroEndosoAltaDialog() {
        log.info("abrirRegistroEndosoAltaDialog - PolizaView");
        this.showRegistrarEndosoAltaDialog = true;
        this.incisoForm = new Inciso();
        this.incisoForm.setPoliza(new Poliza());
        this.incisoForm.getPoliza().setAseguradora(new Aseguradora());
        this.incisoForm.setVehiculo(new Vehiculo());
        this.acuseImportacionList = new ArrayList<>();
        this.showErrorImportacion = false;
        this.layoutFileUpload = null;
        this.importIncisoList = new ArrayList<>();
        this.informacionVehiculo = "";
        PrimeFaces.current().ajax().update("registrar-endoso-alta-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('registrarEndosoAltaDialog').show();");
    }

    public void cerrarRegistroEndosoAltaDialog() {
        log.info("cerrarRegistroEndosoAltaDialog - PolizaView");
        this.showRegistrarEndosoAltaDialog = false;
        this.incisoForm = null;
        this.acuseImportacionList = new ArrayList<>();
        this.showErrorImportacion = false;
        this.layoutFileUpload = null;
        this.importIncisoList = null;
        this.informacionVehiculo = null;
        PrimeFaces.current().ajax().update("registrar-endoso-alta-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarEndosoAltaDialog').hide();");
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_ENDOSO_ALTA", orden = 5,
            nombre = "Endoso de alta", descripcion = "Acción que permite registar un endoso de alta")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_ALTA')")
    public void guardarEndosoAlta() {
        log.info("guardarEndosoAlta - PolizaView");
        try {
            if(this.incisoForm != null) {
                incisoService.generarEndosoAlta(this.incisoForm, userSessionBean.getUserName());
                Messages.addInfo("Se ha guardado correctamente el endoso de alta");

                this.buscar();
                this.verIncisos();
                this.cerrarRegistroEndosoAltaDialog();
            }
        } catch (Exception e) {
            log.error("Error al guardar el endoso de alta", e);
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

    public void buscarVehiculo(String noSerie) {
        log.info("buscarVehiculo - PolizaView");
        try {
            if(noSerie != null) {

                Vehiculo vehiculo = vehiculoService.findByNoSerie(noSerie);

                if(vehiculo.getIncisoVigente() != null) {
                    Messages.addWarn("El vehículo tiene una póliza vigente.");
                    this.informacionVehiculo = "";
                    return;
                }

                this.informacionVehiculo = String.join(" | ",
                        vehiculo.getNoSerie(),
                        vehiculo.getMarca(),
                        vehiculo.getAnio().toString(),
                        vehiculo.getModelo(),
                        vehiculo.getColor(),
                        vehiculo.getDescripcionVehiculo());
            } else {
                this.informacionVehiculo = "";
            }
        } catch (Exception e) {
            log.warn("Error al buscar un vehículo", e);
            Messages.addWarn("No se ha encontrado el vehículo");
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_IMPORTAR_ENDOSO_ALTA", orden = 6,
            nombre = "Importar endoso de alta", descripcion = "Acción que permite importar registros de endoso de alta")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_IMPORTAR_ENDOSO_ALTA')")
    public void importarLayoutEndosoAlta(FileUploadEvent event) {
        log.info("importarLayoutEndosoAlta - PolizaView");
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        byte[] fileContent = file.getContent();

        try {
            // Aquí puedes procesar el archivo según su tipo o contenido
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                // Si es un archivo Excel, procesarlo utilizando Apache POI
                Class<Inciso> incisoClass = Inciso.class;
                List<ConfigHeaderExcelModel> list = new ArrayList<>();
                list.add(ConfigHeaderExcelModel.builder().header("ASEGURADORA").fieldName("polizaIdAseguradora").columnIndex(0).build());
                list.add(ConfigHeaderExcelModel.builder().header("NO. PÓLIZA").fieldName("polizaNoPoliza").columnIndex(1).build());
                list.add(ConfigHeaderExcelModel.builder().header("INCISO").fieldName("numeroInciso").columnIndex(2).build());
                list.add(ConfigHeaderExcelModel.builder().header("NO. SERIE VEHÍCULO").fieldName("vehiculoNoSerie").columnIndex(3).build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA INICIO VIGENCIA").fieldName("fechaInicioVigencia").columnIndex(4).dateFormat("dd/MM/yyyy").build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA FIN VIGENCIA").fieldName("fechaFinVigencia").columnIndex(5).dateFormat("dd/MM/yyyy").build());
                list.add(ConfigHeaderExcelModel.builder().header("FOLIO FACTURA").fieldName("folioFactura").columnIndex(6).build());
                list.add(ConfigHeaderExcelModel.builder().header("COSTO").fieldName("costo").columnIndex(7).build());
                list.add(ConfigHeaderExcelModel.builder().header("FRECUENCIA PAGO").fieldName("frecuenciaPago").columnIndex(8).build());

                ImportExcelFile<Inciso> importExcelFile = new ImportExcelFile<>();
                this.importIncisoList = importExcelFile.processExcelFile(fileContent, incisoClass, list);

                this.layoutFileUpload = fileName;
                PrimeFaces.current().ajax().update("tab-view_endoso_alta:endoso-alta-layout-form:dropZoneLayout");
                log.info("Se ha cargado la información del layout correctamente. Incisos a importar: {}", this.importIncisoList.size());
            } else {
                // Manejar otros tipos de archivos si es necesario
                // Por ejemplo, mostrar un mensaje de error
                Messages.addError("Error", "Tipo de archivo no válido");
            }
        } catch (Exception e) {
            log.error("Ocurrió un error al importar el layout de endoso de alta", e);
            Messages.addError("Ocurrió un error al importar el layout de endoso de alta");
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_IMPORTAR_ENDOSO_ALTA')")
    public void guardarLayoutEndosoAlta() {
        log.info("guardarLayoutEndosoAlta - PolizaView");
        try {
            if(this.importIncisoList != null) {
                this.acuseImportacionList = incisoService.importarEndosoAlta(this.importIncisoList,
                        userSessionBean.getUserName());

                // Si alguno marco error entonces no se guardó nada y se muestra el acuse
                if(acuseImportacionList.stream().anyMatch(a -> a.getError() == 1)) {
                    this.showErrorImportacion = true;
                }
                else {
                    // Si no, entonces se guardó correctamente
                    Messages.addInfo("Se han importado correctamente el layout de endoso de alta.");
                    this.limpiarFiltros();
                    this.buscar();
                    this.verIncisos();
                    this.cerrarRegistroEndosoAltaDialog();
                    PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
                }
            }
        } catch (Exception e) {
            log.error("Error al importar layout de endoso de alta", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_SOLICITAR_PAGO_INCISO", orden = 7,
            nombre = "Solicitar pago de incisos", descripcion = "Acción que permite solicitar el pago de los incisos seleccionados")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_SOLICITAR_PAGO_INCISO')")
    public void solicitarPagoIncisos() {
        log.info("solicitarPagoIncisos - PolizaView");
        try {
            if(this.incisoSelectedList != null) {
                incisoService.solicitarPago(this.incisoSelectedList, userSessionBean.getUserName());
                this.buscar();
                this.verIncisos();
                PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
                Messages.addInfo("Se ha solicitado pago de los incisos seleccionados");
            }
            else {
                Messages.addWarn("No se han seleccionado incisos");
            }
        } catch (Exception e) {
            log.error("Error al solicitar pago incisos", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_RECHAZAR_SOLICITUD", orden = 8,
            nombre = "Rechazar solicitud", descripcion = "Acción que permite rechazar la solicitud de pago de incisos.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_RECHAZAR_SOLICITUD')")
    public void abrirRechazarSolicitudModal() {
        log.info("abrirRechazarSolicitudModal - PolizaView");
        this.showRechazarSolicitudDialog = true;
        this.motivoRechazoSolicitud = "";

        if(this.incisoSelectedList.isEmpty()) {
            Messages.addWarn("No has seleccionado incisos");
            return;
        }

        boolean notEnProcesoPago = this.incisoSelectedList.stream()
                .anyMatch(i -> !Objects.equals(i.getEstatusInciso().getIdEstatusInciso(), ESTATUS_INCISO_EN_PROCESO_PAGO));
        // Si tiene un estatus diferente a EN PROCESO DE PAGO
        if(notEnProcesoPago) {
            Messages.addWarn("Asegúrate de seleccionar incisos EN PROCESO DE PAGO");
            return;
        }

        PrimeFaces.current().ajax().update("rechazar-solicitud-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('rechazarSolicitudDialog').show();");
    }

    public void cerrarRechazarSolicitudModal() {
        log.info("cerrarRechazarSolicitudModal - PolizaView");
        this.showRechazarSolicitudDialog = false;
        this.motivoRechazoSolicitud = "";
        PrimeFaces.current().ajax().update("rechazar-solicitud-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('rechazarSolicitudDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_RECHAZAR_SOLICITUD')")
    public void rechazarSolicitudIncisos() {
        log.info("rechazarSolicitudIncisos - PolizaView");
        try {
            if(this.incisoSelectedList != null && !this.motivoRechazoSolicitud.isEmpty()) {
                incisoService.rechazarSolicitud(this.incisoSelectedList, this.motivoRechazoSolicitud,
                        userSessionBean.getUserName());
                Messages.addInfo("Se ha rechazado la solicutd de pago de los incisos seleccionados");
                this.buscar();
                this.verIncisos();
                this.cerrarRechazarSolicitudModal();
                PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
            }
            else {
                Messages.addWarn("Asegurate de seleccionar incisos y de ingresar un motivo de rechazo.");
            }
        } catch (Exception e) {
            log.error("Error al rechazar solicitud incisos", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_EDITAR_INCISO", orden = 9,
            nombre = "Editar inciso", descripcion = "Acción que permite editar la información de un inciso.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_EDITAR_INCISO')")
    public void abrirEditarIncisoModal(Long idInciso) {
        log.info("abrirEditarIncisoModal - PolizaView");
        try {
            this.showEditarIncisoDialog = true;
            this.incisoForm = incisoService.findById(idInciso);
            PrimeFaces.current().ajax().update("editar-inciso-dialog-content", "growl");
            PrimeFaces.current().executeScript("PF('editarIncisoDialog').show();");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de edición de inciso", e);
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

    public void cerrarEditarIncisoModal() {
        log.info("cerrarEditarIncisoModal - PolizaView");
        this.showEditarIncisoDialog = false;
        this.incisoForm = null;
        PrimeFaces.current().ajax().update("editar-inciso-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('editarIncisoDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_EDITAR_INCISO')")
    public void editarInciso() {
        log.info("editarInciso - PolizaView");
        try {
            if(this.incisoForm != null) {
                incisoService.editar(this.incisoForm, userSessionBean.getUserName());
                Messages.addInfo("Se ha guardado correctamente");
                this.buscar();
                this.verIncisos();
                this.cerrarEditarIncisoModal();
                PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
            }
        } catch (Exception e) {
            log.error("Error editar el inciso", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_REGISTRAR_PAGO", orden = 9,
            nombre = "Registrar pago", descripcion = "Acción que permite registrar el pago de los incisos seleccionados.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_REGISTRAR_PAGO')")
    public void abrirRegistrarPagoModal() {
        log.info("abrirRegistrarPagoModal - PolizaView");
        try {
            this.showRegistrarPagoDialog = true;
            this.file = null;

            if(this.incisoSelectedList.isEmpty()) {
                Messages.addWarn("No has seleccionado incisos");
                return;
            }

            boolean notEnProcesoPago = this.incisoSelectedList.stream()
                    .anyMatch(i -> !Objects.equals(i.getEstatusInciso().getIdEstatusInciso(), ESTATUS_INCISO_EN_PROCESO_PAGO));
            // Si tiene un estatus diferente a EN PROCESO DE PAGO
            if(notEnProcesoPago) {
                Messages.addWarn("Asegúrate de seleccionar incisos EN PROCESO DE PAGO");
                return;
            }

            this.pagoInciso = incisoService.getDetallePagoIncisos(this.incisoSelectedList);

            PrimeFaces.current().ajax().update("registrar-pago-dialog-content", "growl");
            PrimeFaces.current().executeScript("PF('registrarPagoDialog').show();");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de registro de pago", e);
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

    public void cerrarRegistrarPagoModal() {
        log.info("cerrarRegistrarPagoModal - PolizaView");
        this.showRegistrarPagoDialog = false;
        this.pagoInciso = null;
        this.file = null;
        PrimeFaces.current().ajax().update("registrar-pago-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('registrarPagoDialog').hide();");
    }

    public void usarSaldoPendiente() {
        log.info("usarSaldoPendiente - PolizaView");
        if(this.pagoInciso != null) {
            // Se recalcula el saldo
            if(this.pagoInciso.isUsarSaldoPendiente()) {
                double total = this.pagoInciso.getSubtotal() + (this.pagoInciso.getSaldoPendiente());
                if(total < 0) {
                    this.pagoInciso.setTotal(0d);
                }
                else {
                    this.pagoInciso.setTotal(total);
                }
            } else {
                this.pagoInciso.setTotal(this.pagoInciso.getSubtotal());
            }
        }
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_REGISTRAR_PAGO')")
    public void registrarPago() {
        log.info("registrarPago - PolizaView");
        try {
            UploadedFile file = this.file;
            String fileName = file.getFileName();
            byte[] fileContent = file.getContent();
            String filePath = SaveFile.importFileToPath(fileContent, fileName, FOLDER_POLIZAS_FACTURAS);
            this.pagoInciso.setNombreArchivo(fileName);
            this.pagoInciso.setRutaArchivo(filePath);
        } catch (Exception e) {
            log.error("Ocurrio un error al subir la factura.", e);
            Messages.addError("Ocurrió un error al subir la factura. Intente nuevamente, si el problema persiste contacte a soporte.");
            return;
        }

        try {
            this.incisoService.registarPagoIncisos(this.pagoInciso, userSessionBean.getUserName());
            this.buscar();
            this.verIncisos();
            this.cerrarRegistrarPagoModal();
            PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
            Messages.addInfo("Se ha registrado correctamente el pago.");
        } catch (Exception e) {
            log.warn("Error al registrar el pago de incisos", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_ENDOSO_MODIFICACION", orden = 10,
            nombre = "Endoso de modificación", descripcion = "Acción que permite registrar el endoso de modificación de un inciso.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_MODIFICACION')")
    public void abrirEndosoModificacionModal(Long idInciso) {
        log.info("abrirEndosoModificacionModal - PolizaView");
        try {
            this.showEndosoModificacionDialog = true;
            this.file = null;
            Inciso inciso = incisoService.findById(idInciso);
            this.endosoModificacion = EndosoModificacion.builder()
                    .inciso(inciso)
                    .build();

            Vehiculo vehiculo = inciso.getVehiculo();

            this.informacionVehiculo = String.join(" | ",
                    vehiculo.getNoSerie(),
                    vehiculo.getMarca(),
                    vehiculo.getAnio().toString(),
                    vehiculo.getModelo(),
                    vehiculo.getColor(),
                    vehiculo.getDescripcionVehiculo());

            PrimeFaces.current().ajax().update("endoso-modificacion-dialog-content", "growl");
            PrimeFaces.current().executeScript("PF('endosoModificacionDialog').show();");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de endoso de modificacion", e);
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

    public void cerrarEndosoModificacionModal() {
        log.info("cerrarEndosoModificacionModal - PolizaView");
        this.showEndosoModificacionDialog = false;
        this.endosoModificacion = null;
        this.file = null;
        this.informacionVehiculo = "";
        PrimeFaces.current().ajax().update("endoso-modificacion-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('endosoModificacionDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_MODIFICACION')")
    public void guardarEndosoModificacion() {
        log.info("guardarEndosoModificacion - PolizaView");
        try {
            incisoService.generarEndosoModificacion(this.endosoModificacion, userSessionBean.getUserName());
            this.buscar();
            this.verIncisos();
            this.cerrarEndosoModificacionModal();
            PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
            Messages.addInfo("Se ha registrado el endoso de modificación.");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de endoso de modificacion", e);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "POLIZA_WRITE_ENDOSO_BAJA", orden = 11,
            nombre = "Endoso de baja", descripcion = "Acción que permite registrar el endoso de baja de un inciso.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_BAJA')")
    public void abrirEndosoBajaModal(Long idInciso) {
        log.info("abrirEndosoBajaModal - PolizaView");
        try {
            this.showEndosoBajaDialog = true;
            this.file = null;
            Inciso inciso = incisoService.findById(idInciso);
            this.endosoBaja = EndosoBaja.builder()
                    .inciso(inciso)
                    .build();
            Vehiculo vehiculo = inciso.getVehiculo();
            this.informacionVehiculo = String.join(" | ",
                    vehiculo.getNoSerie(),
                    vehiculo.getMarca(),
                    vehiculo.getAnio().toString(),
                    vehiculo.getModelo(),
                    vehiculo.getColor(),
                    vehiculo.getDescripcionVehiculo());
            PrimeFaces.current().ajax().update("endoso-baja-dialog-content", "growl");
            PrimeFaces.current().executeScript("PF('endosoBajaDialog').show();");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de endoso de modificacion", e);
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

    public void cerrarEndosoBajaModal() {
        log.info("cerrarEndosoBajaModal - PolizaView");
        this.showEndosoBajaDialog = false;
        this.endosoBaja = null;
        this.file = null;
        this.informacionVehiculo = "";
        PrimeFaces.current().ajax().update("endoso-modificacion-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('endosoBajaDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_WRITE_ENDOSO_BAJA')")
    public void guardarEndosoBaja() {
        log.info("guardarEndosoBaja - PolizaView");
        try {
            incisoService.generarEndosoBaja(this.endosoBaja, userSessionBean.getUserName());
            this.buscar();
            this.verIncisos();
            this.cerrarEndosoBajaModal();
            PrimeFaces.current().ajax().update("form_datatable", "form_datatable_incisos");
            Messages.addInfo("Se ha registrado el endoso de baja.");
        } catch (Exception e) {
            log.warn("Error al abrir el formulario de endoso de baja", e);
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

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_EXPORTAR_POLIZAS", orden = 3,
            nombre = "Exportar pólizas", descripcion = "Acción que permite descargar los registros de las pólizas filtradas en un formato Excel.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_READ_EXPORTAR_POLIZAS')")
    public ExportFile exportarPolizas() throws IOException {
        log.info("exportarPolizas - PolizaView");

        if(this.polizaList != null && !this.polizaList.isEmpty()) {

            XSSFWorkbook workbook = generatorExcelFile.createWorkbook();

            List<Long> idPolizaList = this.polizaList.stream()
                    .map(Poliza::getIdPoliza)
                    .toList();
            List<Inciso> incisos = incisoService.findByIdPolizaList(idPolizaList);

            List<ExcelCell> cellList = this.getExcelCell(workbook);

            ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                    .data(incisos)
                    .cells(cellList)
                    .sheetName("POLIZAS")
                    .filename("polizas")
                    .autoFilter(true)
                    .agregarFechaGeneracion(true)
                    .appName("SICASY")
                    .build();

            return generatorExcelFile.createExcelFile(workbook, excelDataSheet);
        }

        return new ExportFile();
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "POLIZA_READ_EXPORTAR_POLIZA_INCISO", orden = 4,
            nombre = "Exportar incisos de una póliza", descripcion = "Acción que permite descargar los registros de los incisos de una póliza seleccionada en un formato Excel.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'POLIZA_READ_EXPORTAR_POLIZA_INCISO')")
    public ExportFile exportarIncisos(Poliza poliza) throws IOException {
        log.info("exportarIncisos - PolizaView");

        if(this.polizaList != null && !this.polizaList.isEmpty()) {

            XSSFWorkbook workbook = generatorExcelFile.createWorkbook();

            List<Inciso> incisos = incisoService.findByIdPoliza(poliza.getIdPoliza());

            List<ExcelCell> cellList = this.getExcelCell(workbook);

            ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                    .data(incisos)
                    .cells(cellList)
                    .sheetName("POLIZAS")
                    .filename("polizas")
                    .autoFilter(true)
                    .agregarFechaGeneracion(true)
                    .appName("SICASY")
                    .build();

            return generatorExcelFile.createExcelFile(workbook, excelDataSheet);
        }

        return new ExportFile();
    }


    //region events

    public void onChangeAseguradoraForm() {
        if(this.incisoForm != null && this.incisoForm.getPoliza() != null && this.incisoForm.getPoliza().getAseguradora() != null) {
            this.loadPolizaFormList(this.incisoForm.getPoliza().getAseguradora().getIdAseguradora());
        }
        else {
            this.loadPolizaFormList(null);
        }

    }

    //endregion

    //region private methods

    private void loadAseguradorasList() {
        this.aseguradoraList = aseguradoraService.findAll();
    }

    private void loadPolizaFormList(Integer idAseguradora) {
        if(idAseguradora != null) {
            this.polizaFormList = polizaService.findDropdown(idAseguradora);
        }
        else {
            this.polizaFormList = new ArrayList<>();
        }
    }

    public List<ExcelCell> getExcelCell(XSSFWorkbook workbook) throws IOException {
        log.info("getExcelCell - PolizaView");

        List<ExcelCell> cellList = new ArrayList<>();

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

        XSSFCellStyle centerDateTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                .workbook(workbook)
                .fontSize(12)
                .fontColor(ExcelFontColor.BLACK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                .dataFormat("dd/MM/yyyy")
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

        cellList.add(ExcelCell.builder().columnName("ID").propertyExpression("idInciso").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("ASEGURADORA").propertyExpression("poliza.aseguradora.nombre").cellStyle(leftTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("PÓLIZA").propertyExpression("poliza.numeroPoliza").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("INCISO").propertyExpression("numeroInciso").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("NO. SERIE").propertyExpression("vehiculo.noSerie").cellStyle(leftTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("FECHA INICIO").propertyExpression("fechaInicioVigencia").cellStyle(centerDateTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("FECHA FIN").propertyExpression("fechaFinVigencia").cellStyle(centerDateTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("COSTO").propertyExpression("costo").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("SALDO").propertyExpression("saldo").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("FRECUENCIA PAGO").propertyExpression("frecuenciaPago").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("BAJA PENDIENTE").propertyExpression("bajaPendienteSiniestro == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("TIENE NOTA DE CRÉDITO").propertyExpression("tieneNotaCredito == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("INCISO PAGO").propertyExpression("incisoPagado == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("ESTATUS").propertyExpression("estatusInciso.nombre").cellStyle(centerTopStyle).cellAutoSize(true).build());
        cellList.add(ExcelCell.builder().columnName("OBSERVACIONES").propertyExpression("observaciones").cellStyle(centerTopStyle).cellAutoSize(true).build());

        return cellList;
    }

    //endregion private methods
}
