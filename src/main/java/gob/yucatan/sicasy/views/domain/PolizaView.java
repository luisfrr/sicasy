package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
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
    private @Getter @Setter Poliza polizaFilter;
    private @Getter @Setter Poliza polizaForm;
    private @Getter @Setter Poliza polizaSelected;
    private @Getter List<Poliza> polizaList;
    private @Getter List<Inciso> incisoList;
    private @Getter @Setter List<Inciso> incisoSelectedList;

    private @Getter boolean showPanelPolizas;
    private @Getter boolean showRegistrarPolizasDialog;
    private @Getter boolean showAdjuntarPolizaDialog;

    private @Getter List<Aseguradora> aseguradoraList;

    private @Getter boolean showErrorImportacion;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;
    private @Getter @Setter String layoutFileUpload;
    private @Getter @Setter List<Poliza> importPolizaList;

    private final UserSessionBean userSessionBean;
    private final IPolizaService polizaService;
    private final IAseguradoraService aseguradoraService;


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
            this.polizaSelected = polizaService.findFullById(this.polizaSelected.getIdPoliza());

            if(this.polizaSelected.getIncisoSet() != null &&
                    !this.polizaSelected.getIncisoSet().isEmpty()) {
                this.incisoList =  new ArrayList<>(polizaSelected.getIncisoSet());
            } else  {
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
            this.limpiarFiltros();
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
        PrimeFaces.current().ajax().update("adjuntar-poliza-dialog", "form_datatable", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarPolizaDialog').show();");
    }

    public void cerrarAdjuntarPolizaDialog() {
        log.info("cerrar modal adjuntar poliza");
        this.showAdjuntarPolizaDialog = false;
        this.polizaForm = null;
        PrimeFaces.current().ajax().update("adjuntar-poliza-dialog", "form_datatable");
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
                Messages.addInfo("Se ha guardado correctamente la foto: " + fileName);

            } else {
                Messages.addWarn("No se ha seleccionado el vehículo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }
    }

    //region private methods

    private void loadAseguradorasList() {
        this.aseguradoraList = aseguradoraService.findAll();
    }

    //endregion private methods
}
