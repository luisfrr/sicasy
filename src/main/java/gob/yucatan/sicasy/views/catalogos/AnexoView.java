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
import org.primefaces.model.file.UploadedFile;
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

    private @Getter String title;
    private @Getter String titleDialog;

    private @Getter Anexo anexoFilter;
    private @Getter Anexo anexoSelected;
    private @Getter List<Anexo> anexoList;
    private @Getter EstatusRegistro[] estatusRegistros;
    private @Getter List<Licitacion> licitacionesActivasList;
    private @Getter @Setter UploadedFile anexoFile;

    private final IAnexoService anexoService;
    private final ILicitacionService licitacionService;
    private final UserSessionBean userSessionBean;
    private final IGeneratorExcelFile generatorExcelFile;

    @PostConstruct
    public void init(){
        log.info("Inicializando Anexos View");

        this.title = "Anexos";
        this.anexoSelected = null;
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
                        String pathfile = SaveFile.saveFileToPath(anexoFile.getContent(), anexoFile.getFileName(), "\\Downloads\\");
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
                            String pathfile = SaveFile.saveFileToPath(anexoFile.getContent(), anexoFile.getFileName(), "\\Downloads\\");
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

        cellList.add(ExcelCell.builder().columnName("Anexo").propertyExpression("nombre").cellStyle(centerTopStyle).cellWidth(150).build());
        cellList.add(ExcelCell.builder().columnName("Número Licitación").propertyExpression("numLicitacionString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Descripción").propertyExpression("descripcion").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Fecha de inicio").propertyExpression("fechaInicioString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Fecha de termino").propertyExpression("fechaFinalString()").cellStyle(leftTopStyle).cellWidth(450).build());
        cellList.add(ExcelCell.builder().columnName("Fecha de firma").propertyExpression("fechaFirmaString()").cellStyle(leftTopStyle).cellWidth(450).build());

        ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                .data(this.anexoList)
                .cells(cellList)
                .title("Anexos")
                .sheetName("DATA")
                .filename("Anexos")
                .autoFilter(true)
                .agregarFechaGeneracion(true)
                .appName("SICASY")
                .build();

        return generatorExcelFile.createExcelFile(workbook, excelDataSheet);

    }

    private Boolean validatePuedeGuardarNewAnexo(){

        if (this.anexoSelected != null && this.anexoSelected.getIdAnexo() == null &&
                this.anexoSelected.getNombre() != null) {
            List<Anexo> anexosResult = new ArrayList<>();

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
                if (anexo.get().getLicitacion() != null && licitacionSearch != null
                        && anexo.get().getLicitacion().getNumeroLicitacion()
                            .equals(licitacionSearch.getNumeroLicitacion())
                        && anexo.get().getLicitacion().getEstatusRegistro().equals(EstatusRegistro.ACTIVO)) {
                    return false;
                }
            }

        }

        return true;
    }


}
