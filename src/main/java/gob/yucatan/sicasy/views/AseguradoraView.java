package gob.yucatan.sicasy.views;

import gob.yucatan.sicasy.business.entities.Aseguradora;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
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
public class AseguradoraView {

    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Aseguradora aseguradoraSelected;
    private @Getter Aseguradora aseguradoraFiltroHelper;
    private @Getter List<Aseguradora> aseguradorasList;

    private final IAseguradoraService  aseguradoraService;
    private final UserSessionBean userSessionBean;

    @PostConstruct
    public void init(){
        log.info("Inicializando seguradora");
        this.title = "Aseguradoras";

        this.aseguradoraSelected = null;
        this.limpiarFiltros();

    }

    public void limpiarFiltros(){
        log.info("Limpiando filtros");
        this.aseguradoraFiltroHelper = new Aseguradora();
        this.buscar();
    }

    public void buscar(){
        log.info("Buscando aseguradoras");
        this.aseguradorasList = aseguradoraService.findAllDynamic(this.aseguradoraFiltroHelper);
    }

    public void agregarAseguradora(){
        log.info("Agregando aseguradora");
        this.titleDialog = "Agregar Aseguradora";
        this.aseguradoraSelected = new Aseguradora();

    }

    public void guardarAseguradora(){
        log.info("Guardando aseguradora");

        try {
            if (this.aseguradoraSelected != null) {
                if (this.aseguradoraSelected.getIdAseguradora() != null){
                    // si no es null el id entonces es un update
                    this.aseguradoraSelected.setModificadoPor(userSessionBean.getUserName());
                    this.aseguradoraSelected.setFechaModificacion(new Date());
                    aseguradoraService.update(aseguradoraSelected);
                }else {
                    // id aseguradora null, entonces se inserta nuevo en BD
                    this.aseguradoraSelected.setCreadoPor(userSessionBean.getUserName());
                    this.aseguradoraSelected.setFechaCreacion(new Date());
                    aseguradoraService.save(aseguradoraSelected);
                }

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operación exitosa", "Se ha guardado correctamente la información");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().executeScript("PF('formDialog').hide();");
                this.buscar();
                this.aseguradoraSelected  = null;
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
        Optional<Aseguradora> aseguradoraOptional = aseguradoraService.findById(id);

        if(aseguradoraOptional.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de esta Aseguradora.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().executeScript("PF('formDialog').hide();");
        } else {
            titleDialog = "Editar Aseguradora";
            this.aseguradoraSelected = aseguradoraOptional.get();
        }
    }

    public void doEliminar(Integer id) {
        log.info("prepare to eliminar");
        Optional<Aseguradora> aseguradoraOptional = aseguradoraService.findById(id);
        this.aseguradoraSelected = aseguradoraOptional.orElse(null);

    }

   public void eliminar() {
        log.info("eliminar aseguradora");

        if(this.aseguradoraSelected == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de esta Aseguradora.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Se ha eliminado exitósamente la información");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            aseguradoraService.delete(this.aseguradoraSelected);
            this.aseguradoraSelected = null;
            this.limpiarFiltros();
        }

       PrimeFaces.current().executeScript("PF('confirmDialog').hide();");

   }


}
