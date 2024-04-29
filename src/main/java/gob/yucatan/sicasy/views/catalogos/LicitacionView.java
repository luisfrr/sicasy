package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
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
import gob.yucatan.sicasy.utils.imports.excel.SaveFile;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CATALOGO_LICITACION_VIEW", nombre = "Catálogo de Licitaciones")
public class LicitacionView {

    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Boolean fechaFinalValida;
    private @Getter Licitacion licitacionSelected;
    private @Getter Licitacion licitacionFilter;
    private @Getter List<Licitacion> licitacionList;
    private @Getter EstatusRegistro[] estatusRegistros;
    private @Getter @Setter UploadedFile file;
    private @Getter @Setter DefaultStreamedContent downloadedLicitacionFile;

    private final ILicitacionService licitacionService;
    private final IAnexoService anexoService;
    private final UserSessionBean userSessionBean;
    private final IGeneratorExcelFile generatorExcelFile;


    @PostConstruct
    public void init(){
        log.info("Inicializando Licitaciones View");
        this.title = "Licitaciones";

        this.licitacionSelected = null;
        this.fechaFinalValida = true;
        this.estatusRegistros = EstatusRegistro.values();
        this.limpiarFiltros();

    }

    public void limpiarFiltros(){
        log.info("Limpiando filtros");
        this.licitacionFilter = new Licitacion();
        this.licitacionFilter.setEstatusRegistro(EstatusRegistro.ACTIVO);
        this.buscar();
    }

    public void buscar(){
        log.info("Buscando Licitaciones");
        this.licitacionList = licitacionService.findAllDynamic(this.licitacionFilter).stream()
                .sorted(Comparator.comparing(Licitacion:: getNumeroLicitacion))
                .toList();
    }

    public void agregarLicitacion(){
        log.info("Agregando licitacion nueva");
        this.titleDialog = "Agregar Licitación";
        this.licitacionSelected = new Licitacion();

    }

    public void guardarLicitacion(){
        log.info("Guardando licitacion");

        try {
            if (this.licitacionSelected != null) {
                if (this.licitacionSelected.getIdLicitacion() != null){
                    // si no es null el id entonces es un update
                    this.licitacionSelected.setModificadoPor(userSessionBean.getUserName());
                    this.licitacionSelected.setFechaModificacion(new Date());

                    if (file != null){
                        String pathfile = SaveFile.saveFileToPath(file.getContent(), file.getFileName(), "\\Downloads\\");
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
                    if (optLicitacion.isEmpty()){ // checar si no existe una licitacion con ese numero de licitacion existente y activa
                        this.licitacionSelected.setCreadoPor(userSessionBean.getUserName());
                        this.licitacionSelected.setFechaCreacion(new Date());

                        if (file != null){
                            String pathfile = SaveFile.saveFileToPath(file.getContent(), file.getFileName(), "\\Downloads\\");
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
                        // si encontramos algun dato ya existente entonces indicamos la observacion
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

    public void editar(Integer id) {
        log.info("editar");
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

    public void doEliminar(Integer id) {
        log.info("prepare to eliminar");
        Optional<Licitacion> licitacionOptional = licitacionService.findById(id);
        this.licitacionSelected = licitacionOptional.orElse(null);

    }

    public void eliminar() {
        log.info("eliminar licitacion");

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
        log.info("validando fecha final" );

        if (this.licitacionSelected.getFechaInicio() != null && this.licitacionSelected.getFechaFinal() != null){

            if (this.licitacionSelected.getFechaInicio().after(this.licitacionSelected.getFechaFinal())){
                // la fecha inicio no puede estar dspues de la fecha final
                this.fechaFinalValida = false;
                FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Fecha final inválida."));
            }else
                fechaFinalValida = true;

        }

        PrimeFaces.current().ajax().update("form_dialog:bnt_saveLicitacion ");

    }

    public ExportFile exportFileExcel() throws IOException {
        log.info("exportFileExcel");
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
                .title("Licitaciones")
                .sheetName("DATA")
                .filename("Licitaciones")
                .autoFilter(true)
                .agregarFechaGeneracion(true)
                .appName("SICASY")
                .build();

        return generatorExcelFile.createExcelFile(workbook, excelDataSheet);

    }

    public DefaultStreamedContent downloadLicitacionFile(String filePath) {
        log.info("downloadAnexoFile anexo");
        File fi = new File(filePath);
        try {
            if (!fi.exists()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "No se encontró el archivo.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
            InputStream input = new FileInputStream(fi);
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            downloadedLicitacionFile = DefaultStreamedContent.builder()
                    .name(URLEncoder.encode(fi.getName(), StandardCharsets.UTF_8))
                    .contentType(externalContext.getMimeType(fi.getName()))
                    .stream( () -> input )
                    .build();

            return downloadedLicitacionFile;
        }catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

    }

}

