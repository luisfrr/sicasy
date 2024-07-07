package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
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
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CATALOGO_ANEXO_VIEW", nombre = "Catálogo de Anexos")
public class AnexoView implements Serializable {

    @Value("${app.files.folder.anexos}")
    private @Getter String FOLDER_ANEXO;

    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Boolean fechasValidadasCorrectas;

    private @Getter Anexo anexoFilter;
    private @Getter Anexo anexoSelected;
    private @Getter List<Anexo> anexoList;
    private @Getter @Setter List<Anexo> anexoImportList;
    private @Getter EstatusRegistro[] estatusRegistros;
    private @Getter List<Licitacion> licitacionesActivasList;
    private @Getter @Setter UploadedFile anexoFile;
    private @Getter boolean showErrorImportacion;
    private @Getter String layoutFileUpload;
    private @Getter @Setter List<AcuseImportacion> acuseImportacionList;

    private final IAnexoService anexoService;
    private final ILicitacionService licitacionService;
    private final UserSessionBean userSessionBean;
    private final IGeneratorExcelFile generatorExcelFile;

    @PostConstruct
    public void init(){
        log.info("Inicializando Anexos View");

        this.title = "Anexos";
        this.anexoSelected = null;
        this.fechasValidadasCorrectas = true;
        this.licitacionesActivasList = null;
        this.estatusRegistros = EstatusRegistro.values();
        this.limpiarFiltros();
    }

    public void limpiarFiltros(){
        log.info("Limpiando filtros");
        this.anexoFilter = new Anexo();
        this.anexoFilter.setLicitacion(new Licitacion());
        this.anexoFilter.setEstatusRegistro(EstatusRegistro.ACTIVO);
        this.licitacionesActivasList = licitacionService.findAllLicitacionActive().stream()
                .sorted(Comparator.comparing(Licitacion::getNumeroLicitacion)).toList();
        this.buscar();
    }

    public void buscar(){
        log.info("Buscando Anexos");
        this.anexoList = anexoService.findAllDynamic(this.anexoFilter);
    }

    public void agregarAnexo(){
        log.info("Agregando Anexo");
        this.titleDialog = "Agregar Anexo";
        this.anexoSelected = new Anexo();
        this.anexoSelected.setLicitacion(new Licitacion());

    }

    public void guardarAnexo(){
        log.info("Guardando anexo");

        try {
            if (this.anexoSelected != null) {
                if (this.anexoSelected.getIdAnexo() != null){
                    // si no es null el id entonces es un update
                    this.anexoSelected.setModificadoPor(userSessionBean.getUserName());
                    this.anexoSelected.setFechaModificacion(new Date());

                    if (anexoFile != null){
                        String pathfile = SaveFile.importFileToPath(anexoFile.getContent(), anexoFile.getFileName(), FOLDER_ANEXO);
                        anexoSelected.setRutaArchivo(pathfile);
                    }

                    anexoService.update(anexoSelected);

                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Operación exitosa", "Se ha guardado correctamente la información");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().executeScript("PF('formDialog').hide();");
                    this.buscar();
                    this.anexoSelected  = null;

                }else {
                    // id null, entonces se inserta nuevo en BD
                    if (validatePuedeGuardarNewAnexo()){
                        this.anexoSelected.setCreadoPor(userSessionBean.getUserName());
                        this.anexoSelected.setFechaCreacion(new Date());
                        if (anexoFile != null){
                            String pathfile = SaveFile.importFileToPath(anexoFile.getContent(), anexoFile.getFileName(), FOLDER_ANEXO);
                            anexoSelected.setRutaArchivo(pathfile);
                        }
                        anexoService.save(anexoSelected);

                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Operación exitosa", "Se ha guardado correctamente la información");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        PrimeFaces.current().executeScript("PF('formDialog').hide();");
                        this.buscar();
                        this.anexoSelected  = null;
                    }else {
                        // mostrar mensaje de advertencia que no se puede guardar la informacion
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "El Anexo ya se encuentra registrado con la licitación selecionada."));

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

    public void editar(Long id) {
        log.info("editar");
        Optional<Anexo> anexoOptional = anexoService.findById(id);

        if(anexoOptional.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de este Anexo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().executeScript("PF('formDialog').hide();");
        } else {
            titleDialog = "Editar Anexo";
            this.anexoSelected = anexoOptional.get();
        }
    }

    public void doEliminar(Long id) {
        log.info("prepare to eliminar");
        Optional<Anexo> anexoOptional = anexoService.findById(id);
        this.anexoSelected = anexoOptional.orElse(null);

    }

    public void eliminar() {
        log.info("eliminar Anexo");

        if(this.anexoSelected == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de este Anexo.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Aviso", "Se ha eliminado exitosamente la información");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            this.anexoSelected.setBorradoPor(userSessionBean.getUserName());
            anexoService.delete(this.anexoSelected);
            this.anexoSelected = null;
            this.limpiarFiltros();

        }

        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

    }

    public ExportFile exportFileExcel() throws IOException {
        log.info("exportFileExcel anexo");
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

        cellList.add(ExcelCell.builder().columnName("NUM_LICITACION").propertyExpression("licitacion.numeroLicitacion").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("NUM_ANEXO").propertyExpression("nombre").cellStyle(centerTopStyle).cellWidth(150).build());
        cellList.add(ExcelCell.builder().columnName("DESCRIPCION").propertyExpression("descripcion").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("FECHA_INICIO").propertyExpression("fechaInicioString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("FECHA_FIN").propertyExpression("fechaFinalString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("FECHA_FIRMA").propertyExpression("fechaFirmaString()").cellStyle(leftTopStyle).cellWidth(450).build());

        ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                .data(this.anexoList)
                .cells(cellList)
                .sheetName("ANEXOS")
                .filename("Anexos")
                .autoFilter(true)
                .agregarFechaGeneracion(true)
                .appName("SICASY")
                .build();

        return generatorExcelFile.createExcelFile(workbook, excelDataSheet);
    }

    public void validateFechaInicioFinal(SelectEvent event){
        log.info("validando fecha final" );

        if (this.anexoSelected.getFechaInicio() != null && this.anexoSelected.getFechaFinal() != null){

            if (this.anexoSelected.getFechaInicio().after(this.anexoSelected.getFechaFinal())){
                // la fecha inicio no puede estar dspues de la fecha final
                this.fechasValidadasCorrectas = false;
                FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Fecha selecionada inválida."));
            }else
                fechasValidadasCorrectas = true;

        }

        PrimeFaces.current().ajax().update("form_dialog:btn_guardarAnexo ");

    }

    private Boolean validatePuedeGuardarNewAnexo(){

        if (this.anexoSelected != null && this.anexoSelected.getIdAnexo() == null &&
                this.anexoSelected.getNombre() != null) {
            List<Anexo> anexosResult;

            // buscar si ya existe un anexo con el mismo nombre y si tiene una licitacion activa
            Anexo anexoSearch = new Anexo();
            Optional<Anexo> anexo = Optional.empty();
            anexoSearch.setNombre(this.anexoSelected.getNombre());
            anexoSearch.setEstatusRegistro(EstatusRegistro.ACTIVO);
            anexoSearch.setLicitacion(this.anexoSelected.getLicitacion());
            anexosResult = anexoService.findByNombreAndLicitacion(anexoSearch.getNombre(), anexoSearch.getLicitacion());
            if (anexosResult != null && anexosResult.size() == 1) {
                anexo = Optional.ofNullable(anexosResult.getFirst());
            } else if (anexosResult == null) {
                log.info("Anexos no encontrados o se encontraron mas de un resultado");
                return true;
            }

            Licitacion licitacionSearch = licitacionService.findById(this.anexoSelected.getLicitacion().getIdLicitacion()).orElse(null);

            if(anexo.isPresent() && anexo.get().getEstatusRegistro().equals(EstatusRegistro.ACTIVO)) {
                // si existe el anexo y esta activo, revisar si tiene alguna licitacion activa vinculada
                return anexo.get().getLicitacion() == null || licitacionSearch == null
                        || !anexo.get().getLicitacion().getNumeroLicitacion()
                        .equals(licitacionSearch.getNumeroLicitacion())
                        || !anexo.get().getLicitacion().getEstatusRegistro().equals(EstatusRegistro.ACTIVO);
            }

        }

        return true;
    }

    public void abrirModalImport(){
        this.layoutFileUpload = null;
        this.showErrorImportacion = false;
        this.acuseImportacionList = null;
        //PrimeFaces.current().ajax().update("import-dialog-content");
        PrimeFaces.current().executeScript("PF('formDialogImport').show()");
    }

    public void cerrarModalImport() {
        this.anexoSelected = null;
        //PrimeFaces.current().ajax().update("import-dialog-content");
        PrimeFaces.current().executeScript("PF('formDialogImport').hide()");
    }

    public void importarLayout(FileUploadEvent event) throws IOException {
        UploadedFile file = event.getFile();
        String fileName = file.getFileName();
        byte[] fileContent = file.getContent();

        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            //procesarlo utilizando Apache POI
            Class<Anexo> anexoClass = Anexo.class;
            List<ConfigHeaderExcelModel> list = new ArrayList<>();
            list.add(ConfigHeaderExcelModel.builder().header("NUM_LICITACION").fieldName("numLicitacionString").columnIndex(0).build());
            list.add(ConfigHeaderExcelModel.builder().header("NUM_ANEXO").fieldName("nombre").columnIndex(1).build());
            list.add(ConfigHeaderExcelModel.builder().header("DESCRIPCION").fieldName("descripcion").columnIndex(2).build());
            list.add(ConfigHeaderExcelModel.builder().header("FECHA_INICIO").fieldName("fechaInicio").columnIndex(3).build());
            list.add(ConfigHeaderExcelModel.builder().header("FECHA_FIN").fieldName("fechaFinal").columnIndex(4).build());
            list.add(ConfigHeaderExcelModel.builder().header("FECHA_FIRMA").fieldName("fechaFirma").columnIndex(5).build());


            ImportExcelFile<Anexo> importExcelFile = new ImportExcelFile<>();
            this.anexoImportList = importExcelFile.processExcelFile(fileContent, anexoClass, list);

            this.anexoImportList.forEach(Anexo -> Anexo.setLicitacion(new Licitacion()));

            this.layoutFileUpload = fileName;
            PrimeFaces.current().ajax().update(":form_import:dropZoneLayout");
            log.info("Se ha cargado la información del layout correctamente. Vehículos a importar: {}", this.anexoImportList.size());
        } else {
            Messages.addError("Error", "Tipo de archivo no válido");
        }
    }

    public void guardarImportacionAnexos(){
        log.info("guardar importacion");
        try {
            if(this.anexoImportList != null) {

                this.acuseImportacionList = anexoService.importar(this.anexoImportList, userSessionBean.getUserName());

//                // Si alguno marco error entonces no se guardó nada y se muestra el acuse
                if(acuseImportacionList.stream().anyMatch(a -> a.getError() == 1)) {
                    this.showErrorImportacion = true;
                }
                else {
                    // Si no, entonces se guardó correctamente
                    Messages.addInfo("Todos los anexos se han registrado correctamente");
                    this.limpiarFiltros();
                    this.buscar();
                    this.cerrarModalImport();
                }

            }
        } catch (Exception e) {
            log.error("Error al guardar nuevo anexo", e);
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
