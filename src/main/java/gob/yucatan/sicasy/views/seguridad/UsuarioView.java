package gob.yucatan.sicasy.views.seguridad;

import gob.yucatan.sicasy.business.annotations.ConfigPermiso;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import gob.yucatan.sicasy.business.enums.TipoPermiso;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.services.iface.*;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
@ConfigPermiso(tipo = TipoPermiso.VIEW, codigo = "SEGURIDAD_USUARIOS_VIEW",
        nombre = "Módulo de Usuarios", descripcion = "Permite ver y filtrar la información de usuarios.",
        url = "/views/seguridad/usuarios.faces")
public class UsuarioView implements Serializable {

    private @Getter String title;
    private @Getter String formDialogTitle;
    private @Getter Usuario usuarioSelected;
    private @Getter Usuario usuarioFilter;
    private @Getter List<Usuario> usuarios;
    private @Getter List<EstatusUsuario> estatusUsuarios;
    private @Getter List<Rol> rolList;

    private @Getter boolean showConfigurarPermisos;
    private @Getter Permiso permisoFilter;
    private @Getter List<UsuarioPermiso> usuarioPermisoList;

    private final IUsuarioService usuarioService;
    private final UserSessionBean userSessionBean;
    private final IPermisoScannerService permisoScannerService;
    private final IPermisoService permisoService;
    private final IUsuarioPermisoService usuarioPermisoService;
    private final IRolService rolService;
    private final IEmailService emailService;

    @PostConstruct
    public void init() {
        log.info("Init Usuarios");
        this.title = "Usuarios";

        this.usuarioSelected = null;
        this.showConfigurarPermisos = false;
        this.limpiarFiltros();

        // Si es owner puede filtrar todos los roles
        if(userSessionBean.isOwner()) {
            this.estatusUsuarios = Arrays.stream(EstatusUsuario.values()).toList();
        } else {
            this.estatusUsuarios = Arrays.stream(EstatusUsuario.values())
                    .filter(estatusUsuario -> estatusUsuario != EstatusUsuario.BORRADO)
                    .collect(Collectors.toList());
        }

        this.rolList = rolService.findAllDynamic(Rol.builder().estatus(EstatusRegistro.ACTIVO).build());
    }

    public void limpiarFiltros() {
        log.info("Limpiar Filtros Usuarios");

        // Se inicializan los filtros
        this.usuarioFilter = new Usuario();

        // Se agrega filtro por default
        this.usuarioFilter.setEstatus(EstatusUsuario.ACTIVO);
        this.usuarioFilter.setRolOwner(userSessionBean.isOwner());
        this.usuarioFilter.setIdRolList(new ArrayList<>());

        // Aquí se puede vaciar la lista o buscar todos los registros.
        // Depende la utilidad que se le quiera dar
        this.buscar();
    }

    public void buscar() {
        log.info("Buscar Usuario");
        this.usuarios = usuarioService.findAllDynamic(this.usuarioFilter);
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_NUEVO",
            nombre = "Agregar Usuario", descripcion = "Acción que permite agregar un nuevo usuario")
    public void nuevo() {
        log.info("Nuevo Usuario");
        this.formDialogTitle = "Agregar Usuario";
        this.usuarioSelected = new Usuario();
        this.usuarioSelected.setIdRolList(new ArrayList<>());
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_EDITAR",
            nombre = "Editar Usuario", descripcion = "Acción que permite editar la información básica de un usuario.")
    public void editar(Long id) {
        log.info("Editar Usuario");
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);

        if(usuarioOptional.isEmpty()) {
            Messages.addError("No se ha encontrado la información del usuario.");
        } else {
            formDialogTitle = "Editar Usuario";
            this.usuarioSelected = usuarioOptional.get();
            if(this.usuarioSelected.getUsuarioRolSet() != null) {
                this.usuarioSelected.setIdRolList(this.usuarioSelected.getUsuarioRolSet().stream()
                        .map(usuarioRol -> usuarioRol.getRol().getIdRol())
                        .toList());
            } else {
                this.usuarioSelected.setIdRolList(new ArrayList<>());
            }
        }
    }

    public void guardar() {
        log.info("Guardar Usuario");
        try {
            if(this.usuarioSelected != null) {
                // IdUsuario diferente de null es una edicion
                if(this.usuarioSelected.getIdUsuario() != null) {
                    this.usuarioSelected.setModificadoPor(userSessionBean.getUserName());
                    this.usuarioSelected.setFechaModificacion(new Date());
                    usuarioService.update(this.usuarioSelected);
                } else {
                    // IdUsuario es null es una creacion
                    this.usuarioSelected.setCreadoPor(userSessionBean.getUserName());
                    this.usuarioSelected.setFechaCreacion(new Date());
                    Usuario usuarioCreated = usuarioService.create(this.usuarioSelected);

                    // Enviar correo de activación
                    emailService.sendActivateAccountEmail(usuarioCreated);
                }
                Messages.addInfo("¡Registro existoso!","Se ha enviado el correo de activación de cuenta.");
                PrimeFaces.current().executeScript("PF('formDialog').hide();");
                this.buscar();
            }
        } catch (Exception ex) {
            log.error("Error al guardar Usuario", ex);
            String message;
            if(ex instanceof BadRequestException)
                message = ex.getMessage();
            else if(ex instanceof NotFoundException)
                message = ex.getMessage();
            else
                message = "Ocurrió un error inesperado.";
            Messages.addError(message);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_ELIMINAR",
            nombre = "Eliminar Usuario", descripcion = "Acción que permite borrar un usuario")
    public void eliminar(Long idUsuario) {
        log.info("Eliminar Usuario");
        try {
            usuarioService.delete(Usuario.builder()
                            .idUsuario(idUsuario)
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
            Messages.addError(message);
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SEGURIDAD_USUARIOS_READ_CONFIGURAR_PERMISOS",
            nombre = "Ver Configuración de Permisos", descripcion = "Permite ver el botón que abre el configurador de permisos.")
    public void verConfiguracionPermisos(Usuario usuario) {
        log.info("Ver Configuracion Permisos Usuario");
        this.usuarioSelected = usuario;
        this.showConfigurarPermisos = true;
        this.limpiarFiltrosPermisos();
    }

    public void regresar() {
        log.info("Regresar Usuario");
        this.usuarioSelected = null;
        this.showConfigurarPermisos = false;
        this.limpiarFiltrosPermisos();
    }

    public void buscarPermisos() {
        log.info("Buscar Permisos Usuario");
        this.usuarioPermisoList = usuarioPermisoService.findByUsuario(this.usuarioSelected);
    }

    public void limpiarFiltrosPermisos() {
        log.info("Limpiar Filtros Permisos");
        this.permisoFilter = new Permiso();
        this.usuarioPermisoList = new ArrayList<>();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_ASIGNAR_PERMISOS",
            nombre = "Asignar permiso", descripcion = "Acción que permite habilitar, deshabilitar un permiso al usuario seleccionado.")
    public void asignarPermiso(UsuarioPermiso usuarioPermiso) {
        log.info("Asignar Permiso Usuario");
        try {
            usuarioPermisoService.asignarPermiso(usuarioPermiso, userSessionBean.getUserName());
            this.buscarPermisos();
            Messages.addInfo("¡Listo!", "La configuración se ha guardado correctamente.");
        } catch (Exception e) {
            log.error("Ocurrio un error al guardar el permiso del usuario.", e);
            Messages.addError("Ocurrió un error. Intente de nuevo, si el problema persiste contacte al administrador.");
        }
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_ACTUALIZAR_PERMISOS",
            nombre = "Actualizar permisos", descripcion = "Acción que permite buscar y actualizar los permisos del sistema.")
    public void actualizarPermisos() {
        log.info("Actualizar Permisos Usuario");
        List<Permiso> permisos = permisoScannerService.getPermisos("gob.yucatan.sicasy",
                userSessionBean.getUserName());
        permisoService.updateAll(permisos);
        log.info("Permisos: {}", permisos.size());
        this.buscarPermisos();
    }
}
