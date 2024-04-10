package gob.yucatan.sicasy.views.seguridad;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IPermisoScannerService;
import gob.yucatan.sicasy.services.iface.IPermisoService;
import gob.yucatan.sicasy.services.iface.IRolService;
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
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SEGURIDAD_ROLES_VIEW",
        nombre = "Módulo de Roles", url = "/views/seguridad/roles.faces")
public class RolView {

    private @Getter String title;
    private @Getter Rol rolSelected;
    private @Getter Rol rolFilter;
    private @Getter List<Rol> roles;
    private @Getter String formDialogTitle;

    private final IRolService rolService;
    private final UserSessionBean userSessionBean;
    private final IPermisoScannerService permisoScannerService;
    private final IPermisoService permisoService;

    @PostConstruct
    public void init() {
        log.info("Init RolView");
        this.title = "Roles";

        this.rolSelected = null;
        this.limpiarFiltros();

        List<Permiso> permisos = permisoScannerService.getPermisos("gob.yucatan.sicasy",
                userSessionBean.getUserName());
        permisoService.updateAll(permisos);

        log.info("Permisos: {}", permisos.size());
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros RolView");

        // Se inicializan los filtros
        this.rolFilter = new Rol();

        // Se agrega filtro por default
        this.rolFilter.setEstatus(EstatusRegistro.ACTIVO);

        // Aqui se puede vaciar la lista o buscar todos los registros.
        // Depende la utilidad que se le quiera dar
        this.buscar();
    }

    public void buscar() {
        log.info("buscar RolView");
        this.roles = rolService.findAllDynamic(this.rolFilter);
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_NUEVO",
            nombre = "Nuevo rol", descripcion = "Acción que permite agregar un nuevo rol")
    public void nuevo() {
        log.info("nuevo RolView");
        formDialogTitle = "Nuevo Rol";
        this.rolSelected = new Rol();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_EDITAR",
            nombre = "Editar rol", descripcion = "Acción que permite editar la información de un rol")
    public void editar(Long id) {
        log.info("editar RolView");
        Optional<Rol> rolOptional = rolService.findById(id);

        if(rolOptional.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "No se ha encontrado la información del rol.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            formDialogTitle = "Editar Rol";
            this.rolSelected = rolOptional.get();
        }
    }

    public void guardar() {
        log.info("guardar RolView");
        try {
            if(this.rolSelected != null) {
                // IdRol diferente de null es una edicion
                if(this.rolSelected.getIdRol() != null) {
                    this.rolSelected.setModificadoPor(userSessionBean.getUserName());
                    this.rolSelected.setFechaModificacion(new Date());
                    rolService.update(this.rolSelected);
                } else {
                    // IdRol es null es una creacion
                    this.rolSelected.setCreadoPor(userSessionBean.getUserName());
                    this.rolSelected.setFechaCreacion(new Date());
                    rolService.save(this.rolSelected);
                }
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Operación exitosa", "Se ha guardado correctamente la información del rol");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().executeScript("PF('formDialog').hide();");
                this.buscar();
            }
        } catch (Exception ex) {
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_ELIMINAR",
            nombre = "Eliminar rol", descripcion = "Acción que permite borrar un rol")
    public void eliminar(Long id) {
        log.info("eliminar RolView");
        try {
            rolService.delete(Rol.builder()
                    .idRol(id)
                    .borradoPor(userSessionBean.getUserName())
                    .fechaBorrado(new Date())
                    .build());
            this.buscar();
        } catch (Exception ex) {
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
