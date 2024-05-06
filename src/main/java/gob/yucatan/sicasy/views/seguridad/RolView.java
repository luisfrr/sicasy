package gob.yucatan.sicasy.views.seguridad;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.RolPermiso;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.IPermisoScannerService;
import gob.yucatan.sicasy.services.iface.IPermisoService;
import gob.yucatan.sicasy.services.iface.IRolPermisoService;
import gob.yucatan.sicasy.services.iface.IRolService;
import gob.yucatan.sicasy.views.beans.Messages;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SEGURIDAD_ROLES_VIEW",
        nombre = "Módulo de Roles", descripcion = "Permite ver y filtrar la información de roles.",
        url = "/views/seguridad/roles.faces")
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_VIEW')")
public class RolView {

    private @Getter String title;
    private @Getter String formDialogTitle;
    private @Getter Rol rolSelected;
    private @Getter Rol rolFilter;
    private @Getter List<Rol> roles;
    private @Getter boolean showConfigurarPermisos;
    private @Getter Permiso permisoFilter;
    private @Getter List<RolPermiso> rolPermisoList;
    private @Getter EstatusRegistro[] estatusRegistros;

    private final IRolService rolService;
    private final UserSessionBean userSessionBean;
    private final IPermisoScannerService permisoScannerService;
    private final IPermisoService permisoService;
    private final IRolPermisoService rolPermisoService;

    @PostConstruct
    public void init() {
        log.info("Init RolView");
        this.title = "Roles";

        this.estatusRegistros = EstatusRegistro.values();

        this.rolSelected = null;
        this.showConfigurarPermisos = false;
        this.limpiarFiltros();
    }

    public void limpiarFiltros() {
        log.info("limpiarFiltros RolView");

        // Se inicializan los filtros
        this.rolFilter = new Rol();

        // Se agrega filtro por default
        this.rolFilter.setEstatus(EstatusRegistro.ACTIVO);

        // Aquí se puede vaciar la lista o buscar todos los registros.
        // Depende la utilidad que se le quiera dar
        this.buscar();
    }

    public void buscar() {
        log.info("buscar RolView");
        this.roles = rolService.findAllDynamic(this.rolFilter);
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_NUEVO",
            nombre = "Nuevo rol", descripcion = "Acción que permite agregar un nuevo rol")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_NUEVO')")
    public void nuevo() {
        log.info("nuevo RolView");
        formDialogTitle = "Nuevo Rol";
        this.rolSelected = new Rol();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_EDITAR",
            nombre = "Editar rol", descripcion = "Acción que permite editar la información de un rol")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_EDITAR')")
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

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_NUEVO', 'SEGURIDAD_ROLES_WRITE_EDITAR')")
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
                message = "Ocurrió un error inesperado.";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", message);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_ELIMINAR",
            nombre = "Eliminar rol", descripcion = "Acción que permite borrar un rol")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_ELIMINAR')")
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
                message = "Ocurrió un error inesperado.";
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", message);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SEGURIDAD_ROLES_READ_CONFIGURAR_PERMISOS",
            nombre = "Ver Configuración de Permisos", descripcion = "Permite ver el botón que abre el configurador de permisos.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_READ_CONFIGURAR_PERMISOS')")
    public void verConfiguracionPermisos(Rol rol) {
        this.rolSelected = rol;
        this.showConfigurarPermisos = true;
        this.limpiarFiltrosPermisos();
    }

    public void regresar() {
        this.rolSelected = null;
        this.showConfigurarPermisos = false;
        this.limpiarFiltrosPermisos();
    }

    public void buscarPermisos() {
        log.info("buscarPermisos RolView");
        this.rolPermisoList = rolPermisoService.findByRol(this.rolSelected);
    }

    public void limpiarFiltrosPermisos() {
        log.info("limpiarFiltrosPermisos RolView");
        this.permisoFilter = new Permiso();
        this.rolPermisoList = new ArrayList<>();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_ASIGNAR_PERMISOS",
            nombre = "Asignar permiso", descripcion = "Acción que permite habilitar, deshabilitar un permiso al rol seleccionado.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_ASIGNAR_PERMISOS')")
    public void asignarPermiso(RolPermiso rolPermiso) {
        log.info("asignarPermiso RolView");
        try {
            rolPermisoService.asignarPermiso(rolPermiso, userSessionBean.getUserName());
            this.buscarPermisos();
            Messages.addInfo("¡Listo!", "La configuración se ha guardado correctamente.");
        } catch (Exception e) {
            log.error("Ocurrio un error al guardar el permiso del rol.", e);
            Messages.addError("Ocurrió un error. Intente de nuevo, si el problema persiste contacte al administrador.");
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_ROLES_WRITE_ACTUALIZAR_PERMISOS",
            nombre = "Actualizar permisos", descripcion = "Acción que permite buscar y actualizar los permisos del sistema.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_ROLES_WRITE_ACTUALIZAR_PERMISOS')")
    public void actualizarPermisos() {
        log.info("actualizarPermisos RolView");
        List<Permiso> permisos = permisoScannerService.getPermisos("gob.yucatan.sicasy",
                userSessionBean.getUserName());
        permisoService.updateAll(permisos);
        log.info("Permisos: {}", permisos.size());
        this.buscarPermisos();
        Messages.addInfo("Se ha actualizado el listado de permisos.");
    }

}
