package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.annotations.ConfigPermisoArray;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.export.excel.models.*;
import gob.yucatan.sicasy.utils.export.excel.services.iface.IGeneratorExcelFile;
import gob.yucatan.sicasy.utils.imports.excel.SaveFile;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
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
import org.primefaces.model.ResponsiveOption;
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
import java.util.Map;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SINIESTRO_VIEW",
        nombre = "Módulo de Siniestros", descripcion = "Permite ver y filtrar la información de los siniestros.",
        url = "/views/modulos/siniestros.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_VIEW')")
public class SiniestrosView implements Serializable {

    // Constantes
    @Value("${app.files.folder.siniestros}")
    private @Getter String FOLDER_SINIESTROS;
    @Value("${app.files.folder.siniestros_deducibles}")
    private @Getter String FOLDER_SINIESTROS_DEDUCIBLES;
    private final @Getter String SINIESTRO_RESPONSABLE_ASEGURADO = "ASEGURADO";
    private final @Getter String SINIESTRO_RESPONSABLE_TERCEROS = "TERCEROS";
    private final @Getter Integer ESTATUS_SINIESTRO_REGISTRADO = 1;
    private final @Getter Integer ESTATUS_SINIESTRO_EN_PROCESO_PAGO = 2;
    private final @Getter Integer ESTATUS_SINIESTRO_FINALIZADO = 3;
    private final @Getter String DEDUCIBLE_AUTOSEGUROS = "AUTOSEGUROS";
    private final @Getter String DEDUCIBLE_DEDUCIBLE = "DEDUCIBLE";
    private final @Getter String DEDUCIBLE_PERDIDA_TOTAL = "PERDIDA TOTAL";

    // Inyección de dependencias
    private final UserSessionBean userSessionBean;
    private final ISiniestroService siniestroService;
    private final IEstatusSiniestroService estatusSiniestroService;
    private final IVehiculoService vehiculoService;
    private final ISiniestroFotoService siniestroFotoService;
    private final IBitacoraSiniestroService bitacoraSiniestroService;
    private final IDeducibleService deducibleService;
    private final IGeneratorExcelFile generatorExcelFile;

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter List<Siniestro> siniestroList;
    private @Getter @Setter List<Siniestro> siniestroSelectedList;
    private @Getter List<BitacoraSiniestro> siniestroBitacoraList;
    private @Getter @Setter Siniestro siniestroSelected;
    private @Getter @Setter Siniestro siniestroForm;
    private @Getter @Setter Siniestro siniestroFilter;
    private @Getter @Setter String motivoRechazo;
    private @Getter boolean readOnlyEditForm;
    private @Getter @Setter List<SiniestroFoto> siniestroFotoList;
    private @Getter List<ResponsiveOption> responsiveOptionsGallery;
    private @Getter int activeIndex = 0;
    private @Getter List<EstatusSiniestro> estatusSiniestroList;
    private @Getter List<String> tipoDeducibleList;
    private @Getter @Setter UploadedFile file;

    // Variables para renderizar
    private @Getter boolean showSiniestroListPanel;
    private @Getter boolean showNuevoSiniestroDialog;
    private @Getter boolean showRechazarSolicitudDialog;
    private @Getter boolean showDetalleSiniestroPanel;
    private @Getter boolean showAdjuntarFotos;


    @PostConstruct
    public void init() {
        log.info("init - SiniestrosView");
        this.title = "Siniestros";
        this.limpiarFiltros();
        this.loadResponsiveOptionsGallery();
    }

    @ConfigPermisoArray({
            @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SINIESTRO_READ_VER_BITACORA", orden = 3,
                    nombre = "Ver bitacora", descripcion = "Permite visualizar la bitácora de un siniestro."),
            @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_DESCARGAR_FACTURA_DEDUCIBLE", orden = 11,
                    nombre = "Descargar factura de deducible", descripcion = "Permite descargar la factura del pago de deducible."),
    })
    public void limpiarFiltros() {
        log.info("limpiarFiltros - SiniestrosView");
        this.siniestroSelected = null;
        this.siniestroFilter = new Siniestro();
        this.siniestroFilter.setEstatusSiniestro(new EstatusSiniestro());
        this.siniestroList = new ArrayList<>();

        this.loadEstatusSiniestros();

        this.showSiniestroListPanel = true;
        this.showNuevoSiniestroDialog = false;
        this.showRechazarSolicitudDialog = false;
        this.showDetalleSiniestroPanel = false;
        this.showAdjuntarFotos = false;
        this.siniestroSelected = null;
        this.siniestroForm = null;

        PrimeFaces.current().ajax().update("form_filtros");
    }

    public void buscar() {
        log.info("buscar - SiniestrosView");
        this.siniestroList = siniestroService.findAllDynamic(this.siniestroFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SINIESTRO_READ_EXPORTAR", orden = 1,
            nombre = "Exportar siniestros", descripcion = "Acción que permite exportar los registros de siniestros en un archivo Excel.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_REGISTRO_SINIESTRO')")
    public ExportFile exportarSiniestros() throws IOException {
        log.info("exportarSiniestros - SiniestrosView");
        if(this.siniestroList != null && !this.siniestroList.isEmpty()) {

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

            XSSFCellStyle centerDateTopStyle = generatorExcelFile.createCellStyle(CreateCellStyle.builder()
                    .workbook(workbook)
                    .fontSize(12)
                    .fontColor(ExcelFontColor.BLACK)
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.TOP)
                    .backgroundColor(ExcelBackgroundColor.NO_BG_COLOR)
                    .dataFormat("dd/MM/yyyy hh:mm")
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

            cellList.add(ExcelCell.builder().columnName("ID").propertyExpression(Siniestro_.ID_SINIESTRO).cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. SINIESTRO").propertyExpression(Siniestro_.NO_SINIESTRO_ASEGURADORA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. SINIESTRO SAF").propertyExpression(Siniestro_.NO_SINIESTRO_SA_F).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("FECHA SINIESTRO").propertyExpression(Siniestro_.FECHA_SINIESTRO).cellStyle(centerDateTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("RESPONSABLE").propertyExpression(Siniestro_.RESPONSABLE).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("AJUSTADOR").propertyExpression(Siniestro_.AJUSTADOR_ASEGURADORA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("AJUSTADOR SAF").propertyExpression(Siniestro_.AJUSTADOR_SA_F).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("CAUSA").propertyExpression(Siniestro_.CAUSA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. SERIE").propertyExpression(String.join(".", Siniestro_.VEHICULO, Vehiculo_.NO_SERIE)).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MARCA").propertyExpression(String.join(".", Siniestro_.VEHICULO, Vehiculo_.MARCA)).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MODELO").propertyExpression(String.join(".", Siniestro_.VEHICULO, Vehiculo_.MODELO)).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("AÑO").propertyExpression(String.join(".", Siniestro_.VEHICULO, Vehiculo_.ANIO)).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("REPORTA").propertyExpression(Siniestro_.REPORTA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("FECHA REPORTE").propertyExpression(Siniestro_.FECHA_REPORTE).cellStyle(centerDateTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DECLARACIÓN").propertyExpression(Siniestro_.DECLARACION_SINIESTRO).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("CONDUCTOR").propertyExpression(Siniestro_.CONDUCTOR).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("NO. LICENCIA").propertyExpression(Siniestro_.NO_LICENCIA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("TIPO LICENCIA").propertyExpression(Siniestro_.TIPO_LICENCIA).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("VENCIMIENTO LICENCIA").propertyExpression(Siniestro_.VENCIMIENTO_LICENCIA).cellStyle(centerDateTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("DAÑO VÍA PÚBLICA").propertyExpression(Siniestro_.DANIO_VIA_PUBLICA + " == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("CORRALÓN").propertyExpression(Siniestro_.CORRALON + " == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MULTA").propertyExpression(Siniestro_.MULTA_VEHICULO + " == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("ESTADO").propertyExpression(Siniestro_.ESTADO).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("MUNICIPIO").propertyExpression(Siniestro_.MUNICIPIO).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("LOCALIDAD").propertyExpression(Siniestro_.LOCALIDAD).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("ESTATUS SINIESTRO").propertyExpression(String.join(".", Siniestro_.ESTATUS_SINIESTRO, EstatusSiniestro_.NOMBRE)).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("OBSERVACIONES").propertyExpression(Siniestro_.OBSERVACIONES).cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("PERDIDA TOTAL").propertyExpression(Siniestro_.PERDIDA_TOTAL + " == 1 ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("REQUIERE PAGO DE DEDUCIBLE").propertyExpression("requierePagoDeducible() ? 'Sí' : 'No'").cellStyle(centerTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("VEHÍCULO VALOR FACTURA").propertyExpression("requierePagoDeducible() ? deducible.vehiculoValorFactura : ''").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("VEHÍCULO VALOR ACTUAL").propertyExpression("requierePagoDeducible() ? deducible.valorActual : ''").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("PÓLIZA").propertyExpression("requierePagoDeducible() ? deducible.polizaNumero : ''").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("INCISO").propertyExpression("requierePagoDeducible() ? deducible.incisoNumero : ''").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("TIPO DEDUCIBLE").propertyExpression("requierePagoDeducible() ? (deducible.tienePolizaVigente == 1 ? 'Sí' : 'No')  : ''").cellStyle(leftTopStyle).cellAutoSize(true).build());
            cellList.add(ExcelCell.builder().columnName("COSTO TOTAL DEDUCIBLE").propertyExpression("requierePagoDeducible() ? deducible.costoTotalDeducible : ''").cellStyle(centerFloatTopStyle).cellAutoSize(true).build());

            ExcelDataSheet excelDataSheet = ExcelDataSheet.builder()
                    .data(this.siniestroList)
                    .cells(cellList)
                    .sheetName("SINIESTRO")
                    .filename("siniestros")
                    .autoFilter(true)
                    .agregarFechaGeneracion(true)
                    .appName("SICASY")
                    .build();

            return generatorExcelFile.createExcelFile(workbook, excelDataSheet);
        }

        return new ExportFile();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_REGISTRAR_SINIESTRO", orden = 1,
            nombre = "Registrar siniestro", descripcion = "Acción que permite registrar un nuevo siniestro.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_REGISTRO_SINIESTRO')")
    public void abrirRegistroSiniestroDialog() {
        log.info("abrirRegistroSiniestroDialog - SiniestrosView");
        this.showNuevoSiniestroDialog = true;
        this.siniestroForm = new Siniestro();
        this.siniestroForm.setVehiculo(new Vehiculo());
        PrimeFaces.current().ajax().update("registrar-siniestro-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarSiniestroDialog').show()");
    }

    public void cerrarRegistroSiniestroDialog() {
        log.info("cerrarRegistroSiniestroDialog - SiniestrosView");
        this.showNuevoSiniestroDialog = false;
        this.siniestroForm = null;
        PrimeFaces.current().ajax().update("registrar-siniestro-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarSiniestroDialog').hide()");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_REGISTRO_SINIESTRO')")
    public void guardarRegistroSiniestro() {
        log.info("guardarRegistroPoliza - SiniestrosView");
        try {
            if(this.siniestroForm != null) {
                siniestroService.registrarNuevoSiniestro(this.siniestroForm, userSessionBean.getUserName());
                Messages.addInfo("Se ha guardado correctamente la póliza");
                this.limpiarFiltros();
                this.buscar();
                this.cerrarRegistroSiniestroDialog();
            }
        } catch (Exception e) {
            log.error("Error al registrar siniestro", e);
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
        log.info("buscar vehiculos - SiniestrosView");
        try {
            if(noSerie != null) {
                Vehiculo vehiculo = vehiculoService.findByNoSerie(noSerie);
                this.siniestroForm.setVehiculo(vehiculo);
            } else {
                this.siniestroForm = new Siniestro();
                this.siniestroForm.setVehiculo(new Vehiculo());
            }
            PrimeFaces.current().ajax().update("registro-siniestro-form:vehiculo_marca",
                    "registro-siniestro-form:vehiculo_anio",
                    "registro-siniestro-form:vehiculo_modelo",
                    "registro-siniestro-form:vehiculo_placa",
                    "registro-siniestro-form:vehiculo_color",
                    "registro-siniestro-form:vehiculo_dependencia",
                    "registro-siniestro-form:vehiculo_poliza",
                    "registro-siniestro-form:vehiculo_inciso");
        } catch (Exception e) {
            log.warn("No se ha encontrado el vehiculo con el No. Serie: {}", noSerie, e);
            Messages.addWarn("No se ha encontrado el vehículo");
        }
    }

    public void vaciarMulta() {
        if(this.siniestroSelected != null) {
            this.siniestroSelected.setCostoMulta(null);
        }
        if(this.siniestroForm != null) {
            this.siniestroForm.setCostoMulta(null);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_SOLICITAR_PAGO_DEDUCIBLE", orden = 6,
            nombre = "Solicitar pago de deducible", descripcion = "Acción que permite solicitar el pago de deducible de los registros seleccionados que requieren pago de deducible.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_SOLICITAR_PAGO_DEDUCIBLE')")
    public void solicitarPagoDeducible() {
        log.info("solicitarPagoDeducible - SiniestrosView");
        try {
            List<Long> idSiniestroSelectedList = this.getIdSiniestroSelectedList();
            siniestroService.solicitarPagoDeducible(idSiniestroSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se ha solicitado el pago de deducible correctamente.");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_AUTORIZAR_PAGO_DEDUCIBLE", orden = 7,
            nombre = "Autorizar pago de deducible", descripcion = "Acción que permite autorizar el pago de deducible de los registros seleccionados.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_AUTORIZAR_PAGO_DEDUCIBLE')")
    public void autorizarPagoDeducible() {
        log.info("autorizarPagoDeducible - SiniestrosView");
        try {
            List<Long> idSiniestroSelectedList = this.getIdSiniestroSelectedList();
            siniestroService.autorizarPagoDeducible(idSiniestroSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se autorizó correctamente el pago de deducible.");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_FINALIZAR_REGISTRO", orden = 8,
            nombre = "Finalizar registro", descripcion = "Acción que permite finalizar el registro de los siniestros seleccionados que no requieren pago de deducible.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_FINALIZAR_REGISTRO')")
    public void finalizarRegistro() {
        log.info("finalizarRegistro - SiniestrosView");
        try {
            List<Long> idSiniestroSelectedList = this.getIdSiniestroSelectedList();
            siniestroService.finalizarRegistro(idSiniestroSelectedList, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se han finalizado correctamente los registros seleccionados.");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_RECHAZAR_SOLICITUD", orden = 9,
            nombre = "Finalizar registro", descripcion = "Acción que permite rechazar la solicitud de pago de deducible de los registros seleccionados.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_RECHAZAR_SOLICITUD')")
    public void abrirRechazarSolicitudDialog() {
        log.info("abrirRechazarSolicitudDialog - SiniestrosView");
        this.showRechazarSolicitudDialog = true;
        this.motivoRechazo = "";
        PrimeFaces.current().ajax().update("rechazar-solicitud-dialog-content");
        PrimeFaces.current().executeScript("PF('rechazarSolicitudDialog').show()");
    }

    public void cerrarRechazarSolicitudDialog() {
        log.info("cerrarRechazarSolicitudDialog - SiniestrosView");
        this.showRechazarSolicitudDialog = false;
        this.motivoRechazo = null;
        PrimeFaces.current().ajax().update("rechazar-solicitud-dialog-content");
        PrimeFaces.current().executeScript("PF('rechazarSolicitudDialog').hide()");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_RECHAZAR_SOLICITUD')")
    public void rechazarSolicitud() {
        log.info("rechazarSolicitud - SiniestrosView");
        try {
            List<Long> idSiniestroSelectedList = this.getIdSiniestroSelectedList();
            siniestroService.rechazarSolicitud(idSiniestroSelectedList, this.motivoRechazo, userSessionBean.getUserName());
            this.buscar();
            Messages.addInfo("Se ha rechazado correctamente las solicitudes de los registros seleccionados.");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    public void regresar() {
        log.info("regresar - SiniestrosView");
        try {
            this.showSiniestroListPanel = true;
            this.showDetalleSiniestroPanel = false;
            PrimeFaces.current().ajax().update("container");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_VER_DETALLE", orden = 2,
            nombre = "Ver detalle", descripcion = "Acción que permite visualizar la información del siniestro.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_VER_DETALLE')")
    public void verDetalle(Siniestro siniestro) {
        log.info("verDetalle - SiniestrosView");
        try {
            this.showSiniestroListPanel = false;
            this.showDetalleSiniestroPanel = true;
            this.siniestroSelected = siniestroService.findById(siniestro.getIdSiniestro());
            this.siniestroFotoList = siniestroFotoService.getSiniestroFotos(this.siniestroSelected.getIdSiniestro());
            this.siniestroBitacoraList = bitacoraSiniestroService.findBySiniestroId(siniestro.getIdSiniestro());
            this.readOnlyEditForm = true;
            this.loadTipoDeducible();
            PrimeFaces.current().ajax().update("container");
        } catch (Exception e) {
            log.warn(e.getMessage());
            Messages.addError(e.getMessage());
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_EDITAR", orden = 3,
            nombre = "Editar", descripcion = "Acción que permite editar la información del siniestro.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_EDITAR')")
    public void permitirEditar() {
        log.info("permitirEditar - SiniestrosView");
        this.readOnlyEditForm = false;
        PrimeFaces.current().ajax().update("tab_view_detalles:edit_siniestro_form");
    }

    public void cancelarEdicion() {
        log.info("cancelarEdicion - SiniestrosView");
        this.siniestroSelected = siniestroService.findById(this.siniestroSelected.getIdSiniestro());
        this.readOnlyEditForm = true;
        PrimeFaces.current().ajax().update("tab_view_detalles:edit_siniestro_form");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_EDITAR')")
    public void guardarEdicion() {
        log.info("guardarEdicion - SiniestrosView");
        try {
            siniestroService.editar(this.siniestroSelected, userSessionBean.getUserName());
            this.siniestroSelected = siniestroService.findById(this.siniestroSelected.getIdSiniestro());
            this.readOnlyEditForm = true;
            Messages.addInfo("Se ha actualizado correctamente la información del siniestro");
            PrimeFaces.current().ajax().update("tab_view_detalles:edit_siniestro_form", "growl");
        } catch (Exception e) {
            log.error("Error al guardar la información del siniestro", e);

            if(e instanceof BadRequestException) {
                Messages.addError(e.getMessage());
            } else {
                Messages.addError("No se ha logrado guardar la información del siniestro.");
            }

            PrimeFaces.current().ajax().update("tab_view_detalles:edit_siniestro_form", "growl");
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_ADJUNTAR_FOTOS", orden = 4,
            nombre = "Adjuntar fotos", descripcion = "Acción que permite adjuntar fotos del siniestro.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_ADJUNTAR_FOTOS')")
    public void abrirModalAdjuntarFotos() {
        log.info("abrirModalAdjuntarFotos - SiniestrosView");
        this.showAdjuntarFotos = true;
        PrimeFaces.current().ajax().update("form_adjuntar_fotos", "growl");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').show();");
    }

    public void cerrarModalAdjuntarFotos() {
        log.info("cerrarModalAdjuntarFotos - SiniestrosView");
        this.showAdjuntarFotos = false;
        PrimeFaces.current().ajax().update("form_adjuntar_fotos");
        PrimeFaces.current().executeScript("PF('adjuntarFotosDialog').hide();");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_ADJUNTAR_FOTOS')")
    public void subirFotoSiniestro(FileUploadEvent event) {
        log.info("subirFotoSiniestro - SiniestrosView");
        String fileName = event.getFile().getFileName();
        try {
            if(this.siniestroSelected != null) {
                String filePath = SaveFile.importFileToPath(event.getFile().getContent(), fileName, FOLDER_SINIESTROS);

                SiniestroFoto siniestroFoto = SiniestroFoto.builder()
                        .siniestro(this.siniestroSelected)
                        .rutaArchivo(filePath)
                        .nombreArchivo(fileName)
                        .fechaCreacion(new Date())
                        .creadoPor(userSessionBean.getUserName())
                        .borrado(0)
                        .build();

                siniestroFotoService.guardarFoto(siniestroFoto);
                Messages.addInfo("Se ha guardado correctamente la foto: " + fileName);

                this.siniestroFotoList = siniestroFotoService.getSiniestroFotos(this.siniestroSelected.getIdSiniestro());
                PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria");

            } else {
                Messages.addWarn("No se ha seleccionado el vehículo");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Messages.addError("No se ha logrado guardar la foto: " + fileName);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_BORRAR_FOTOS", orden = 5,
            nombre = "Editar", descripcion = "Acción que permite adjuntar fotos del siniestro.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_BORRAR_FOTOS')")
    public void borrarFoto(Long idSiniestroFoto) {
        log.info("borrarFoto - SiniestrosView");
        try {
            siniestroFotoService.borrarFoto(idSiniestroFoto, userSessionBean.getUserName());
            this.siniestroFotoList = siniestroFotoService.getSiniestroFotos(this.siniestroSelected.getIdSiniestro());
            PrimeFaces.current().ajax().update("tab_view_detalles:form_galeria");
            Messages.addInfo("Se ha eliminado correctamente la foto");
        } catch (Exception e) {
            Messages.addError(e.getMessage());
        }
    }

    public void changeActiveIndex() {
        log.info("changeActiveIndex - SiniestrosView");
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        this.activeIndex = Integer.parseInt(params.get("index"));
        log.info("activeIndex {}", this.activeIndex);
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SINIESTRO_READ_VER_PANEL_FOTOS", orden = 2,
            nombre = "Editar", descripcion = "Visualizar el panel de fotos donde se encuentra la galeria de fotos del siniestros y las acciones para adjuntar fotos y borrarlas.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_READ_VER_PANEL_FOTOS')")
    public void presentGallery() {
        log.info("presentGallery - SiniestrosView");
        this.activeIndex = 0;
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_GUARDAR_PAGO_DEDUCIBLE", orden = 10,
            nombre = "Adjuntar fotos", descripcion = "Acción que permite guardar la información del pago de deducible.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_WRITE_GUARDAR_PAGO_DEDUCIBLE')")
    public void guardarPagoDeducible() {
        log.info("guardarPagoDeducible - SiniestrosView");
        try {
            if(this.siniestroSelected != null && this.siniestroSelected.getDeducible() != null) {

                if(this.file != null) {
                    try {
                        UploadedFile file = this.file;
                        String fileName = file.getFileName();
                        byte[] fileContent = file.getContent();
                        String filePath = SaveFile.importFileToPath(fileContent, fileName, FOLDER_SINIESTROS_DEDUCIBLES);
                        this.siniestroSelected.getDeducible().setNombreArchivoFactura(fileName);
                        this.siniestroSelected.getDeducible().setRutaArchivoFactura(filePath);

                    } catch (Exception e) {
                        log.error("Ocurrio un error al subir la factura.", e);
                        Messages.addError("Ocurrió un error al subir la factura. Intente nuevamente, si el problema persiste contacte a soporte.");
                        return;
                    }
                } else {
                    if(this.siniestroSelected.getDeducible().getRutaArchivoFactura() == null) {
                        Messages.addError("Se debe subir el archivo de la factura.");
                        return;
                    }
                }

                try {
                    this.siniestroSelected.getDeducible().setSiniestro(this.siniestroSelected);
                    deducibleService.update(this.siniestroSelected,
                            this.siniestroSelected.getDeducible(),
                            userSessionBean.getUserName());
                    this.siniestroSelected = siniestroService.findById(this.getSiniestroSelected().getIdSiniestro());
                    PrimeFaces.current().ajax().update("deducible_form");
                    Messages.addInfo("Se ha registrado correctamente el pago.");
                } catch (Exception e) {
                    log.warn("Error al guardar el pago de deducible", e);
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

    //region privates

    private void loadEstatusSiniestros() {
        this.estatusSiniestroList = estatusSiniestroService.findAll();
    }

    private List<Long> getIdSiniestroSelectedList() {
        if(this.siniestroSelectedList.isEmpty())
            throw new BadRequestException("No has seleccionado ningún registro.");

        return siniestroSelectedList.stream()
                .map(Siniestro::getIdSiniestro)
                .toList();
    }

    private void loadResponsiveOptionsGallery() {
        this.responsiveOptionsGallery = new ArrayList<>();
        responsiveOptionsGallery.add(new ResponsiveOption("1024px", 5));
        responsiveOptionsGallery.add(new ResponsiveOption("768px", 3));
        responsiveOptionsGallery.add(new ResponsiveOption("560px", 1));
    }

    private void loadTipoDeducible() {
        if(this.siniestroSelected != null && this.siniestroSelected.getPerdidaTotal() == 0) {
            this.tipoDeducibleList = List.of(DEDUCIBLE_AUTOSEGUROS, DEDUCIBLE_DEDUCIBLE);
        } else if(this.siniestroSelected != null && this.siniestroSelected.getPerdidaTotal() == 1) {
            this.tipoDeducibleList = List.of(DEDUCIBLE_PERDIDA_TOTAL);
        } else {
            tipoDeducibleList = new ArrayList<>();
        }
    }

    //endregion privates

}
