package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.annotations.ConfigPermisoArray;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.utils.imports.excel.SaveFile;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.ResponsiveOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

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
    public ExportFile exportarSiniestros() {
        return null;
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_SOLICITAR_PAGO_DEDUCIBLE", orden = 2,
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_AUTORIZAR_PAGO_DEDUCIBLE", orden = 3,
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_FINALIZAR_REGISTRO", orden = 4,
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SINIESTRO_WRITE_RECHAZAR_SOLICITUD", orden = 5,
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
            Messages.addError(e.getMessage());
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
