package gob.yucatan.sicasy.views.modulos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.EstatusSiniestro;
import gob.yucatan.sicasy.business.entities.Siniestro;
import gob.yucatan.sicasy.business.entities.Vehiculo;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IEstatusSiniestroService;
import gob.yucatan.sicasy.services.iface.ISiniestroService;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import gob.yucatan.sicasy.utils.export.ExportFile;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SINIESTRO_VIEW",
        nombre = "Módulo de Siniestros", descripcion = "Permite ver y filtrar la información de los siniestros.",
        url = "/views/modulos/siniestros.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SINIESTRO_VIEW')")
public class SiniestrosView implements Serializable {

    // Servicios/Beans
    private final UserSessionBean userSessionBean;
    private final ISiniestroService siniestroService;
    private final IEstatusSiniestroService estatusSiniestroService;
    private final IVehiculoService vehiculoService;

    // Constantes
    private final @Getter String SINIESTRO_RESPONSABLE_ASEGURADO = "ASEGURADO";
    private final @Getter String SINIESTRO_RESPONSABLE_TERCEROS = "TERCEROS";

    // Variables Generales
    private @Getter String title;
    private @Getter @Setter List<Siniestro> siniestroList;
    private @Getter @Setter List<Siniestro> siniestroSelectedList;
    private @Getter @Setter Siniestro siniestroSelected;
    private @Getter @Setter Siniestro siniestroForm;
    private @Getter @Setter Siniestro siniestroFilter;
    private @Getter @Setter String motivoRechazo;

    // Variables selects
    private @Getter List<EstatusSiniestro> estatusSiniestroList;

    // Variables para renderizar
    private @Getter boolean showSiniestroListPanel;
    private @Getter boolean showNuevoSiniestroPanel;
    private @Getter boolean showRechazarSolicitudDialog;



    @PostConstruct
    public void init() {
        log.info("init - SiniestrosView");
        this.title = "Siniestros";
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros - SiniestrosView");
        this.siniestroSelected = null;
        this.siniestroFilter = new Siniestro();
        this.siniestroFilter.setEstatusSiniestro(new EstatusSiniestro());
        this.siniestroList = new ArrayList<>();

        this.loadEstatusSiniestros();

        this.showSiniestroListPanel = true;
        this.siniestroSelected = null;
        this.siniestroForm = null;

        PrimeFaces.current().ajax().update("form_filtros");
    }

    public void buscar() {
        log.info("buscar - SiniestrosView");
        this.siniestroList = siniestroService.findAllDynamic(this.siniestroFilter);
        PrimeFaces.current().ajax().update("form_datatable");
    }

    public ExportFile exportarSiniestros() {
        return null;
    }

    public void abrirRegistroSiniestroDialog() {
        log.info("abrirRegistroSiniestroDialog - SiniestrosView");
        this.showNuevoSiniestroPanel = true;
        this.siniestroForm = new Siniestro();
        this.siniestroForm.setVehiculo(new Vehiculo());
        PrimeFaces.current().ajax().update("registrar-siniestro-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarSiniestroDialog').show()");
    }

    public void cerrarRegistroSiniestroDialog() {
        log.info("cerrarRegistroSiniestroDialog - SiniestrosView");
        this.showNuevoSiniestroPanel = false;
        this.siniestroForm = null;
        PrimeFaces.current().ajax().update("registrar-siniestro-dialog-content");
        PrimeFaces.current().executeScript("PF('registrarSiniestroDialog').hide()");
    }

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
        this.siniestroForm.setCostoMulta(null);
    }

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

    //endregion privates

}
