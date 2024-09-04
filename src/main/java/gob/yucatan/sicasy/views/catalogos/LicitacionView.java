package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.annotations.ConfigPermisoArray;
import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.Anexo;
import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAnexoService;
import gob.yucatan.sicasy.services.iface.ILicitacionService;
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
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CAT_LICITACION_VIEW",
        nombre = "Catálogo de Licitaciones",
        descripcion = "Permite ver y filtrar registros del catálogo de licitaciones.",
        url = "/views/catalogos/licitaciones.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_VIEW')")
public class LicitacionView {

    // Constantes
    @Value("${app.files.folder.licitaciones}")
    private @Getter String FOLDER_LICITACION;

    @Value("${app.files.folder.layouts.importar-licitacion}")
    private @Getter String LAYOUT_LICITACION;

    // Inyección de dependencias
    private final ILicitacionService licitacionService;
    private final IAnexoService anexoService;
    private final UserSessionBean userSessionBean;
    private final IGeneratorExcelFile generatorExcelFile;

    // Variables Generales
    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Boolean fechaFinalValida;
    private @Getter Licitacion licitacionSelected;
    private @Getter Licitacion licitacionFilter;
    private @Getter List<Licitacion> licitacionList;
    private @Getter @Setter List<Licitacion> licitacionImportList;
    private @Getter EstatusRegistro[] estatusRegistros;
    private @Getter @Setter UploadedFile file;
    private @Getter String layoutFileUpload;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;

    // Variables para renderizar
    private @Getter boolean showErrorImportacion;


    @PostConstruct
    public void init(){
        log.info("PostConstruct - LicitacionView");
        this.title = "Licitaciones";

        this.licitacionSelected = null;
        this.fechaFinalValida = true;
        this.estatusRegistros = EstatusRegistro.values();
        this.limpiarFiltros();

    }

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "CAT_LICITACION_READ_DESCARGAR_ARCHIVO", orden = 1,
                    nombre = "Descargar archivo", descripcion = "Permite descargar el archivo de la licitación."),
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "CAT_LICITACION_READ_EXPORTAR_LAYOUT", orden = 3,
                    nombre = "Exportar layout de importación", descripcion = "Permite exportar el archivo layout para importación de licitaciones."),
    })
    public void limpiarFiltros() {
        log.info("limpiarFiltros - LicitacionView");
        this.licitacionFilter = new Licitacion();
        this.licitacionFilter.setEstatusRegistro(EstatusRegistro.ACTIVO);
        this.buscar();
    }

    public void buscar() {
        log.info("buscar - LicitacionView");
        this.licitacionList = licitacionService.findAllDynamic(this.licitacionFilter).stream()
                .sorted(Comparator.comparing(Licitacion:: getNumeroLicitacion))
                .toList();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_LICITACION_WRITE_AGREGAR", orden = 1,
            nombre = "Agregar", descripcion = "Acción que permite agregar una nueva licitación")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_AGREGAR')")
    public void agregarLicitacion() {
        log.info("agregarLicitacion - LicitacionView");
        this.titleDialog = "Agregar Licitación";
        this.licitacionSelected = new Licitacion();

    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_AGREGAR', 'CAT_LICITACION_WRITE_EDITAR')")
    public void guardarLicitacion(){
        log.info("guardarLicitacion - LicitacionView");

        try {
            if (this.licitacionSelected != null) {
                if (this.licitacionSelected.getIdLicitacion() != null){
                    // si no es null el id entonces es un update
                    this.licitacionSelected.setModificadoPor(userSessionBean.getUserName());
                    this.licitacionSelected.setFechaModificacion(new Date());

                    if (file != null){
                        String pathfile = SaveFile.importFileToPath(file.getContent(), file.getFileName(), FOLDER_LICITACION);
                        licitacionSelected.setRutaArchivo(pathfile);
                    }

                    licitacionService.update(licitacionSelected);

                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Operación exitosa", "Se ha guardado correctamente la información");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().executeScript("PF('formDialog').hide();");
                    this.buscar();
                    this.licitacionSelected  = null;
                }else {
                    // id null, entonces se inserta nuevo en BD

                    Optional<Licitacion> optLicitacion = licitacionService.findByNumeroLicitacion(licitacionSelected.getNumeroLicitacion());
                    if (optLicitacion.isEmpty()){ // checar si no existe una licitacion con ese número de licitacion existente y activa
                        this.licitacionSelected.setCreadoPor(userSessionBean.getUserName());
                        this.licitacionSelected.setFechaCreacion(new Date());

                        if (file != null){
                            String pathfile = SaveFile.importFileToPath(file.getContent(), file.getFileName(), FOLDER_LICITACION);
                            licitacionSelected.setRutaArchivo(pathfile);
                        }

                        licitacionService.save(licitacionSelected);

                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Operación exitosa", "Se ha guardado correctamente la información");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        PrimeFaces.current().executeScript("PF('formDialog').hide();");
                        this.buscar();
                        this.licitacionSelected  = null;
                    }else {
                        // si encontramos algún dato ya existente entonces indicamos la observacion
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Atención", "Ya existe una licitación con ese numero de licitación guardado!");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                    }

                }


            }
        }catch (Exception ex) {
            String message;
            if(ex instanceof BadRequestException)
                message = ex.getMessage();
            else if(ex instanceof NotFoundException)
                message = ex.getMessage();
            else
                message = "Ocurrió un error innesperado.";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", message);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }

    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_LICITACION_WRITE_EDITAR", orden = 2,
            nombre = "Editar", descripcion = "Acción que permite editar la información de una licitación")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_EDITAR')")
    public void editar(Integer id) {
        log.info("editar - LicitacionView");
        Optional<Licitacion> licitacionOptional = licitacionService.findById(id);

        if(licitacionOptional.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de esta Licitación.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().executeScript("PF('formDialog').hide();");
        } else {
            titleDialog = "Editar Licitación";
            this.licitacionSelected = licitacionOptional.get();
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_LICITACION_WRITE_ELIMINAR", orden = 3,
            nombre = "Eliminar", descripcion = "Acción que permite eliminar un registro de licitación")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_ELIMINAR')")
    public void doEliminar(Integer id) {
        log.info("doEliminar - LicitacionView");
        Optional<Licitacion> licitacionOptional = licitacionService.findById(id);
        this.licitacionSelected = licitacionOptional.orElse(null);

    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_ELIMINAR')")
    public void eliminar() {
        log.info("eliminar - LicitacionView");

        if(this.licitacionSelected == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de esta Licitación.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else {

            // revisar que no existan anexos activos con la licitacion vinculada je
            List<Anexo> anexos;
            Licitacion licitacionFilter = new Licitacion();
            licitacionFilter.setEstatusRegistro(EstatusRegistro.ACTIVO);
            licitacionFilter.setIdLicitacion(this.licitacionSelected.getIdLicitacion());

            anexos = anexoService.findByLicitacion(licitacionFilter);

            if (anexos.isEmpty()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Aviso", "Se ha eliminado exitosamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                this.licitacionSelected.setBorradoPor(userSessionBean.getUserName());
                licitacionService.delete(this.licitacionSelected);
                this.licitacionSelected = null;
                this.limpiarFiltros();
            }else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Atención", "No se pueden eliminar Licitaciones con Anexos Activos agregados");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }

        }

        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

    }

    public void validateFechaInicioFinal(SelectEvent event){
        log.info("validateFechaInicioFinal - LicitacionView");

        if (this.licitacionSelected.getFechaInicio() != null && this.licitacionSelected.getFechaFinal() != null){

            if (this.licitacionSelected.getFechaInicio().after(this.licitacionSelected.getFechaFinal())){
                // la fecha de inicio no puede estar dspues de la fecha final
                this.fechaFinalValida = false;
                FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Fecha selecionada inválida."));
            }else
                fechaFinalValida = true;

        }

        PrimeFaces.current().ajax().update("form_dialog:bnt_saveLicitacion ");

    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "CAT_LICITACION_READ_EXPORTAR", orden = 1,
            nombre = "Eliminar", descripcion = "Acción que permite exportar los registros de licitación en un archivo Excel.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_READ_EXPORTAR')")
    public ExportFile exportFileExcel() throws IOException {
        log.info("exportFileExcel - LicitacionView");
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

        XSSFCellStyle leftTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                .workbook(workbook)
                .fontSize(12)
                .fontColor(ExcelFontColor.BLACK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                .isWrapText(true)
                .build());

        cellList.add(ExcelCell.builder().columnName("Licitación").propertyExpression("nombre").cellStyle(centerTopStyle).cellWidth(150).build());
        cellList.add(ExcelCell.builder().columnName("Número Licitación").propertyExpression("numeroLicitacion").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Descripción").propertyExpression("descripcion").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Fecha de inicio").propertyExpression("fechaInicioString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Fecha de termino").propertyExpression("fechaFinalString()").cellStyle(leftTopStyle).cellWidth(450).build());

        ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                .data(this.licitacionList)
                .cells(cellList)
                .sheetName("DATA")
                .filename("Licitaciones")
                .autoFilter(true)
                .agregarFechaGeneracion(true)
                .appName("SICASY")
                .build();

        return generatorExcelFile.createExcelFile(workbook, excelDataSheet);

    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_LICITACION_WRITE_IMPORT", orden = 4,
            nombre = "Eliminar", descripcion = "Acción que permite exportar los registros de licitación en un archivo Excel.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_IMPORT')")
    public void abrirModalImport() {
        log.info("abrirModalImport - LicitacionView");
        this.layoutFileUpload = null;
        this.showErrorImportacion = false;
        this.acuseImportacionList = null;
        PrimeFaces.current().executeScript("PF('formDialogImport').show()");
    }

    public void cerrarModalImport() {
        log.info("cerrarModalImport - LicitacionView");
        this.licitacionSelected = null;
        PrimeFaces.current().executeScript("PF('formDialogImport').hide()");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_IMPORT')")
    public void importarLayout(FileUploadEvent event) {
        try {
            log.info("importarLayout - LicitacionView");
            UploadedFile file = event.getFile();
            String fileName = file.getFileName();
            byte[] fileContent = file.getContent();

            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                //procesarlo utilizando Apache POI
                Class<Licitacion> licitacionClass = Licitacion.class;
                List<ConfigHeaderExcelModel> list = new ArrayList<>();
                list.add(ConfigHeaderExcelModel.builder().header("NUM_LICITACION").fieldName("numeroLicitacion").columnIndex(0).build());
                list.add(ConfigHeaderExcelModel.builder().header("NOMBRE").fieldName("nombre").columnIndex(1).build());
                list.add(ConfigHeaderExcelModel.builder().header("DESCRIPCION").fieldName("descripcion").columnIndex(2).build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA_INICIO").fieldName("fechaInicio").columnIndex(3).build());
                list.add(ConfigHeaderExcelModel.builder().header("FECHA_FIN").fieldName("fechaFinal").columnIndex(4).build());

                ImportExcelFile<Licitacion> importExcelFile = new ImportExcelFile<>();
                this.licitacionImportList = importExcelFile.processExcelFile(fileContent, licitacionClass, list);

                this.layoutFileUpload = fileName;
                PrimeFaces.current().ajax().update(":form_import:dropZoneLayout");
                log.info("Se ha cargado la información del layout correctamente. Registros a importar: {}", this.licitacionImportList.size());
            } else {
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

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_LICITACION_WRITE_IMPORT')")
    public void guardarImportacion(){
        log.info("guardarImportacion - LicitacionView");
        try {
            if(this.licitacionImportList != null) {

                this.acuseImportacionList = licitacionService.importar(this.licitacionImportList, userSessionBean.getUserName());

//                // Si alguno marco error entonces no se guardó nada y se muestra el acuse
                if(acuseImportacionList.stream().anyMatch(a -> a.getError() == 1)) {
                    this.showErrorImportacion = true;
                }
                else {
                    // Si no, entonces se guardó correctamente
                    Messages.addInfo("Todos las licitaciones se han registrado correctamente");
                    this.limpiarFiltros();
                    this.buscar();
                    this.cerrarModalImport();
                }

            }
        } catch (Exception e) {
            log.error("Error al guardar nueva licitacion", e);
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

}

