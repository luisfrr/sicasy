package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Licitacion;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.ILicitacionService;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CATALOGO_LICITACION_VIEW", nombre = "Catálogo de Licitaciones")
public class LicitacionView {

    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Licitacion licitacionSelected;
    private @Getter Licitacion licitacionFilter;
    private @Getter List<Licitacion> licitacionList;

    private final ILicitacionService licitacionService;
    private final UserSessionBean userSessionBean;


    @PostConstruct
    public void init(){
        log.info("Inicializando Licitaciones View");
        this.title = "Licitaciones";

        this.licitacionSelected = null;
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
        this.licitacionList = licitacionService.findAllDynamic(this.licitacionFilter);
    }

    public void agregarLicitacion(){
        log.info("Agregando licitacion nueva");
        this.titleDialog = "Agregar Licitacion";
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
                    licitacionService.update(licitacionSelected);
                }else {
                    // id null, entonces se inserta nuevo en BD
                    this.licitacionSelected.setCreadoPor(userSessionBean.getUserName());
                    this.licitacionSelected.setFechaCreacion(new Date());
                    licitacionService.save(licitacionSelected);
                }

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operación exitosa", "Se ha guardado correctamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().executeScript("PF('formDialog').hide();");
                this.buscar();
                this.licitacionSelected  = null;
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Aviso", "Se ha eliminado exitósamente la información");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            licitacionService.delete(this.licitacionSelected);
            this.licitacionSelected = null;
            this.limpiarFiltros();
        }

        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

    }



}
