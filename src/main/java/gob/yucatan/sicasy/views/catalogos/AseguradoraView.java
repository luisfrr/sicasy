package gob.yucatan.sicasy.views.catalogos;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Aseguradora;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "CAT_ASEGURADORA_VIEW",
        nombre = "Catálogo de Aseguradoras",
        descripcion = "Permite ver y filtrar registros del catálogo de aseguradoras.",
        url = "/views/catalogos/aseguradoras.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_VIEW')")
public class AseguradoraView {

    // Inyección de dependencias
    private final IAseguradoraService  aseguradoraService;
    private final UserSessionBean userSessionBean;

    // Variables Generales
    private @Getter String title;
    private @Getter String titleDialog;
    private @Getter Aseguradora aseguradoraSelected;
    private @Getter Aseguradora aseguradoraFiltroHelper;
    private @Getter List<Aseguradora> aseguradorasList;
    private @Getter EstatusRegistro[] estatusRegistros;


    @PostConstruct
    public void init(){
        log.info("PostConstruct - AseguradoraView");
        this.title = "Aseguradoras";

        this.aseguradoraSelected = null;
        this.estatusRegistros = EstatusRegistro.values();
        this.limpiarFiltros();
    }

    public void limpiarFiltros(){
        log.info("limpiarFiltros - AseguradoraView");
        this.aseguradoraFiltroHelper = new Aseguradora();
        this.aseguradoraFiltroHelper.setEstatus(EstatusRegistro.ACTIVO);
        this.buscar();
    }

    public void buscar(){
        log.info("buscar - AseguradoraView");
        this.aseguradorasList = aseguradoraService.findAllDynamic(this.aseguradoraFiltroHelper);
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_ASEGURADORA_WRITE_AGREGAR", orden = 1,
            nombre = "Agregar", descripcion = "Acción que permite agregar una nueva aseguradora")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_WRITE_AGREGAR')")
    public void agregarAseguradora() {
        log.info("agregarAseguradora - AseguradoraView");
        this.titleDialog = "Agregar Aseguradora";
        this.aseguradoraSelected = new Aseguradora();

    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_WRITE_AGREGAR', 'CAT_ASEGURADORA_WRITE_EDITAR')")
    public void guardarAseguradora(){
        log.info("guardarAseguradora - AseguradoraView");
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_ASEGURADORA_WRITE_EDITAR", orden = 2,
            nombre = "Editar", descripcion = "Acción que permite editar la información de una aseguradora")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_WRITE_EDITAR')")
    public void editar(Integer id) {
        log.info("editar - AseguradoraView");
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "CAT_ASEGURADORA_WRITE_ELIMINAR", orden = 3,
            nombre = "Eliminar", descripcion = "Acción que permite eliminar un registro de aseguradora")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_WRITE_ELIMINAR')")
    public void doEliminar(Integer id) {
        log.info("doEliminar - AseguradoraView");
        Optional<Aseguradora> aseguradoraOptional = aseguradoraService.findById(id);
        this.aseguradoraSelected = aseguradoraOptional.orElse(null);

    }

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'CAT_ASEGURADORA_WRITE_ELIMINAR')")
   public void eliminar() {
        log.info("eliminar - AseguradoraView");
        if(this.aseguradoraSelected == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información de esta Aseguradora.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Aviso", "Se ha eliminado exitosamente la información");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            this.aseguradoraSelected.setBorradoPor(userSessionBean.getUserName());
            aseguradoraService.delete(this.aseguradoraSelected);
            this.aseguradoraSelected = null;
            this.limpiarFiltros();
        }
       PrimeFaces.current().executeScript("PF('confirmDialog').hide();");
   }


}
