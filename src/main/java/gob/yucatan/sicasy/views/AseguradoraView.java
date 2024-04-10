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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class AseguradoraView {

    private final Object session;
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


}
