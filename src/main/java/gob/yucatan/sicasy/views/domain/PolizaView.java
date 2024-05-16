package gob.yucatan.sicasy.views.domain;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.dtos.GrupoPoliza;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import gob.yucatan.sicasy.utils.imports.excel.ConfigHeaderExcelModel;
import gob.yucatan.sicasy.utils.imports.excel.ImportExcelFile;
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
    private @Getter @Setter GrupoPoliza grupoPolizaSelected;
    private @Getter List<GrupoPoliza> grupoPolizaList;
    private @Getter List<Poliza> polizaList;
    private @Getter @Setter List<Poliza> polizaSelectedList;
    private @Getter @Setter Poliza polizaSelected;

    private @Getter boolean showPanelPolizas;
    private @Getter boolean showRegistrarPolizasDialog;

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
        this.title = "Polizas";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiar filtros de vehiculos");
        this.polizaFilter = new Poliza();
        this.polizaFilter.setAseguradora(new Aseguradora());
        this.polizaFilter.setEstatusPoliza(new EstatusPoliza());
        this.polizaFilter.setVehiculo(new Vehiculo());

        this.showPanelPolizas = true;
        this.showRegistrarPolizasDialog = false;

        this.loadAseguradorasList();

        this.grupoPolizaList = new ArrayList<>();
        this.polizaList = new ArrayList<>();
        this.grupoPolizaSelected = null;
        this.polizaSelectedList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_filtros", "form_datatable");
    }

    public void buscar() {
        log.info("buscar grupos polizas");
        this.grupoPolizaList = polizaService.findGrupoPoliza(this.polizaFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }

    public void verIncisos() {
        log.info("ver incisos polizas");
        if(this.grupoPolizaSelected != null) {
            this.polizaList = polizaService.findByGrupoPoliza(this.grupoPolizaSelected);
            PrimeFaces.current().ajax().update("form_datatable_incisos");
        }
    }

    public void limpiarIncisos() {
        log.info("limpiar incisos polizas");
        this.polizaList = new ArrayList<>();
        PrimeFaces.current().ajax().update("form_datatable_incisos");
    }

    public void abrirRegistroPolizasDialog() {
        log.info("abrir registro polizas dialog");
        this.showRegistrarPolizasDialog = true;
        this.polizaSelected = new Poliza();
        this.polizaSelected.setAseguradora(new Aseguradora());
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
        this.polizaSelected = null;
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
            if(this.polizaSelected != null) {
                this.polizaSelected.setCreadoPor(userSessionBean.getUserName());
                polizaService.guardarRegistroPoliza(this.polizaSelected);
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

    public void importarLayoutRegistroPolizas(FileUploadEvent event) throws IOException {
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
                list.add(ConfigHeaderExcelModel.builder().header("NO_POLIZA").fieldName("numeroPoliza").columnIndex(1).build());

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
                this.acuseImportacionList = polizaService.importarLayoutRegistroPoliza(this.importPolizaList,
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


    //region private methods

    private void loadAseguradorasList() {
        this.aseguradoraList = aseguradoraService.findAll();
    }

    //endregion private methods
}
