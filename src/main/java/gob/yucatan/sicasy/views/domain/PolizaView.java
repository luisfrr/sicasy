package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IIncisoService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import gob.yucatan.sicasy.services.impl.IncisoServiceImpl;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class PolizaView implements Serializable {

    @Value("${app.files.folder.layouts.importar-vehiculo}")
    private @Getter String LAYOUT_POLIZAS;

    @Value("${app.files.folder.polizas}")
    private @Getter String FOLDER_POLIZAS;

    private @Getter final Integer ESTATUS_INCISO_REGISTRADA = 1;
    private @Getter final Integer ESTATUS_INCISO_EN_PROCESO_PAGO = 2;
    private @Getter final Integer ESTATUS_INCISO_PAGADA = 3;
    private @Getter final Integer ESTATUS_INCISO_BAJA = 4;

    // Generales
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

    private @Getter boolean showPanelPolizas;
    private @Getter boolean showRegistrarPolizasDialog;
    private @Getter boolean showAdjuntarPolizaDialog;
    private @Getter boolean showRegistrarEndosoAltaDialog;
    private @Getter boolean showRechazarSolicitudDialog;
    private @Getter boolean showEditarIncisoDialog;

    private @Getter List<Aseguradora> aseguradoraList;
    private @Getter List<Poliza> polizaFormList;
    private @Getter String informacionVehiculo;

    private @Getter boolean showErrorImportacion;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;
    private @Getter @Setter String layoutFileUpload;
    private @Getter @Setter List<Poliza> importPolizaList;
    private @Getter @Setter List<Inciso> importIncisoList;

    private final UserSessionBean userSessionBean;
    private final IPolizaService polizaService;
    private final IAseguradoraService aseguradoraService;
    private final IIncisoService incisoService;
    private final IVehiculoService vehiculoService;


    @PostConstruct
    public void init() {
        this.title = "Pólizas";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiar filtros de polizas");
        this.polizaFilter = new Poliza();
        this.polizaFilter.setAseguradora(new Aseguradora());

        this.showPanelPolizas = true;
        this.showRegistrarPolizasDialog = false;
        this.showAdjuntarPolizaDialog = false;
        this.showRechazarSolicitudDialog = false;
        this.showEditarIncisoDialog = false;

        this.loadAseguradorasList();

        this.polizaList = new ArrayList<>();
        this.incisoList = new ArrayList<>();
        this.polizaSelected = null;
        this.incisoSelectedList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros", "form_datatable");
    }

    public void buscar() {
        log.info("buscar grupos polizas");
        this.polizaList = polizaService.findAll(this.polizaFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }

    public void verIncisos() {
        log.info("ver incisos polizas");
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
        log.info("limpiar incisos polizas");
        this.incisoList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_datatable_incisos");
    }

    public void abrirRegistroPolizasDialog() {
        log.info("abrir registro polizas dialog");
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
        log.info("cerrar registro polizas dialog");
        this.showRegistrarPolizasDialog = false;
        this.polizaForm = null;
        this.acuseImportacionList = new ArrayList<>();
        this.showErrorImportacion = false;
        this.layoutFileUpload = null;
        this.importPolizaList = null;
        PrimeFaces.current().ajax().update("registrar-polizas-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarPolizasDialog').hide()");
    }

    public void guardarRegistroPoliza() {
        log.info("guardar registro poliza");
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

    public void importarLayoutRegistroPolizas(FileUploadEvent event) {
        log.info("importar layout registro polizas");
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

    public void guardarLayoutRegistroPolizas() {
        log.info("guardar layout registro polizas");
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

    public void eliminarPoliza(Long polizaId) {
        log.info("eliminar poliza");
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

    public void abrirAdjuntarPolizaDialog(Long idPoliza) {
        log.info("abrir modal adjuntar poliza");
        this.showAdjuntarPolizaDialog = true;
        this.polizaForm = polizaService.findById(idPoliza);
        PrimeFaces.current().ajax().update("form_adjuntar_poliza", "form_datatable", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarPolizaDialog').show();");
    }

    public void cerrarAdjuntarPolizaDialog() {
        log.info("cerrar modal adjuntar poliza");
        this.showAdjuntarPolizaDialog = false;
        this.polizaForm = null;
        PrimeFaces.current().ajax().update("form_adjuntar_poliza", "form_datatable");
        PrimeFaces.current().executeScript("PF('adjuntarPolizaDialog').hide();");
    }

    public void adjuntarPoliza(FileUploadEvent event) {
        log.info("adjuntar poliza");
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

    public void abrirRegistroEndosoAltaDialog() {
        log.info("abrir modal registrar endoso de alta");
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
        log.info("cerrar modal registrar endoso de alta");
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

    public void guardarEndosoAlta() {
        log.info("guardar endoso de alta");
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

    public void buscarVehiculo() {
        log.info("buscar vehiculos");
        try {
            if(this.incisoForm != null && this.incisoForm.getVehiculo() != null) {

                if(this.incisoForm.getVehiculo().getNoSerie() != null) {

                    Vehiculo vehiculo = vehiculoService.findByNoSerie(this.incisoForm.getVehiculo().getNoSerie());

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
            }
        } catch (Exception e) {
            log.warn("Error al buscar un vehículo", e);
            Messages.addWarn("No se ha encontrado el vehículo");
        }
    }

    public void importarLayoutEndosoAlta(FileUploadEvent event) {
        log.info("importar layout endoso alta");
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
                list.add(ConfigHeaderExcelModel.builder().header("INCISO").fieldName("inciso").columnIndex(2).build());
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

    public void guardarLayoutEndosoAlta() {
        log.info("guardar layout endoso alta");
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

    public void solicitarPagoIncisos() {
        log.info("solicitar pago incisos");
        try {
            if(this.incisoSelectedList != null) {
                incisoService.solicitarPago(this.incisoSelectedList, userSessionBean.getUserName());
                this.verIncisos();
                PrimeFaces.current().ajax().update("form_datatable_incisos");
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

    public void abrirRechazarSolicitudModal() {
        log.info("abrir rechazar solicitud modal");
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
        log.info("cerrar rechazar solicitud modal");
        this.showRechazarSolicitudDialog = false;
        this.motivoRechazoSolicitud = "";
        PrimeFaces.current().ajax().update("rechazar-solicitud-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('rechazarSolicitudDialog').hide();");
    }

    public void rechazarSolicitudIncisos() {
        log.info("rechazar solicitud incisos");
        try {
            if(this.incisoSelectedList != null && !this.motivoRechazoSolicitud.isEmpty()) {
                incisoService.rechazarSolicitud(this.incisoSelectedList, this.motivoRechazoSolicitud,
                        userSessionBean.getUserName());
                Messages.addInfo("Se ha rechazado la solicutd de pago de los incisos seleccionados");
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

    public void abrirEditarIncisoModal(Long idInciso) {
        log.info("abrir editar inciso modal");
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
        log.info("cerrar editar inciso modal");
        this.showEditarIncisoDialog = false;
        this.incisoForm = null;
        PrimeFaces.current().ajax().update("editar-inciso-dialog-content", "growl");
        PrimeFaces.current().executeScript("PF('editarIncisoDialog').hide();");
    }

    public void editarInciso() {
        log.info("editar inciso");
        try {
            if(this.incisoForm != null) {
                incisoService.editar(this.incisoForm, userSessionBean.getUserName());
                Messages.addInfo("Se ha guardado correctamente");
                this.verIncisos();
                this.cerrarEditarIncisoModal();
                PrimeFaces.current().ajax().update("form_datatable_incisos");
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

    //endregion private methods
}
