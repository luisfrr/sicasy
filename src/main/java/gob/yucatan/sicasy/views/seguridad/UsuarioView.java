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
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_VIEW')")
public class UsuarioView implements Serializable {

    // Inyección de dependencias
    private final IUsuarioService usuarioService;
    private final UserSessionBean userSessionBean;
    private final IPermisoScannerService permisoScannerService;
    private final IPermisoService permisoService;
    private final IUsuarioPermisoService usuarioPermisoService;
    private final IRolService rolService;
    private final IBitacoraUsuarioService bitacoraUsuarioService;
    private final IEmailService emailService;

    // Variables Generales
    private @Getter String title;
    private @Getter String formDialogTitle;
    private @Getter Usuario usuarioSelected;
    private @Getter Usuario usuarioFilter;
    private @Getter List<Usuario> usuarios;
    private @Getter List<EstatusUsuario> estatusUsuarios;
    private @Getter List<Rol> rolList;
    private @Getter Permiso permisoFilter;
    private @Getter List<UsuarioPermiso> usuarioPermisoList;
    private @Getter List<BitacoraUsuario> bitacoraUsuarioList;

    // Variables para renderizar
    private @Getter boolean showConfigurarPermisos;
    private @Getter boolean showPanelPrincipal;
    private @Getter boolean showPanelBitacoraUsuario;
    private @Getter boolean formEdit;


    @PostConstruct
    public void init() {
        log.info("PostConstruct - UsuarioView");
        this.title = "Usuarios";

        this.usuarioSelected = null;
        this.showConfigurarPermisos = false;
        this.showPanelBitacoraUsuario = false;
        this.showPanelPrincipal = true;
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
        log.info("limpiarFiltros - UsuarioView");

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
        log.info("buscar - UsuarioView");
        this.usuarios = usuarioService.findAllDynamic(this.usuarioFilter);
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_NUEVO",
            nombre = "Agregar Usuario", descripcion = "Acción que permite agregar un nuevo usuario")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_NUEVO')")
    public void nuevo() {
        log.info("nuevo - UsuarioView");
        this.formDialogTitle = "Agregar Usuario";
        this.formEdit = false;
        this.usuarioSelected = new Usuario();
        this.usuarioSelected.setIdRolList(new ArrayList<>());
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_EDITAR",
            nombre = "Editar Usuario", descripcion = "Acción que permite editar la información básica de un usuario.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_EDITAR')")
    public void editar(Long id) {
        log.info("editar - UsuarioView");
        Optional<Usuario> usuarioOptional = usuarioService.findById(id);

        if(usuarioOptional.isEmpty()) {
            Messages.addError("No se ha encontrado la información del usuario.");
        } else {
            formDialogTitle = "Editar Usuario";
            this.formEdit = true;
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

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_NUEVO', 'SEGURIDAD_USUARIOS_WRITE_EDITAR')")
    public void guardar() {
        log.info("guardar - UsuarioView");
        try {
            if(this.usuarioSelected != null) {
                // IdUsuario diferente de null es una edicion
                if(this.usuarioSelected.getIdUsuario() != null) {
                    this.usuarioSelected.setModificadoPor(userSessionBean.getUserName());
                    this.usuarioSelected.setFechaModificacion(new Date());
                    usuarioService.update(this.usuarioSelected);

                    Messages.addInfo("¡Listo!","Se ha guardado correctamente.");
                    PrimeFaces.current().executeScript("PF('formDialog').hide();");
                    this.buscar();
                } else {
                    // IdUsuario es null es una creacion
                    this.usuarioSelected.setCreadoPor(userSessionBean.getUserName());
                    this.usuarioSelected.setFechaCreacion(new Date());
                    Usuario usuarioCreated = usuarioService.create(this.usuarioSelected);

                    // Enviar correo de activación
                    emailService.sendActivateAccountEmail(usuarioCreated);

                    Messages.addInfo("¡Registro existoso!","Se ha enviado el correo de activación de cuenta.");
                    PrimeFaces.current().executeScript("PF('formDialog').hide();");
                    this.buscar();
                }
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
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_ELIMINAR')")
    public void eliminar(Long idUsuario) {
        log.info("eliminar - UsuarioView");
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_RESTABLECER_CONTRASENIA",
            nombre = "Reestablecer contraseña",
            descripcion = "Acción que envía un correo para que el usuario genere su nueva contraseña. Borra la anterior contraseña.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_RESTABLECER_CONTRASENIA')")
    public void restablecerContrasenia(Long idUsuario) {
        log.info("restablecerContrasenia - UsuarioView");
        try {
            Usuario usuario = usuarioService.restablecerPassword(idUsuario, userSessionBean.getUserName());
            // Enviar correo de activación
            emailService.sendResetPasswordEmail(usuario);
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_HABILITAR_CUENTA",
            nombre = "Habilitar cuenta de usuario",
            descripcion = "Acción que habilitar la cuenta de usuario. Cambia a estatus Bloqueado, " +
                    "por lo que no podrá ingresar al sistema.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_HABILITAR_CUENTA')")
    public void habilitar(Long idUsuario) {
        log.info("habilitar - UsuarioView");
        try {
            usuarioService.habilitarCuenta(idUsuario, userSessionBean.getUserName());
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

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_DESHABILITAR_CUENTA",
            nombre = "Deshabilitar cuenta de usuario",
            descripcion = "Acción que deshabilitar la cuenta de usuario. Cambia a estatus Activo, " +
                    "lo que significa que podrá ingresar al sistema (siempre que tenga su correo confirmado).")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_DESHABILITAR_CUENTA')")
    public void deshabilitar(Long idUsuario) {
        log.info("deshabilitar - UsuarioView");
        try {
            usuarioService.deshabilitarCuenta(idUsuario, userSessionBean.getUserName());
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
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_READ_CONFIGURAR_PERMISOS')")
    public void verConfiguracionPermisos(Usuario usuario) {
        log.info("verConfiguracionPermisos - UsuarioView");
        this.usuarioSelected = usuario;
        this.showConfigurarPermisos = true;
        this.showPanelPrincipal = false;
        this.limpiarFiltrosPermisos();
    }

//    @ConfigPermiso(tipo = TipoPermiso.READ, codigo = "SEGURIDAD_USUARIOS_READ_VER_BITACORA_USUARIO",
//            nombre = "Ver bitácoras de usuarios", descripcion = "Permite ver las bitácoras del usuario.")
//    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_READ_VER_BITACORA_USUARIO')")
    public void verPanelBitacoraUsuario(Usuario usuario) {
        log.info("verPanelBitacoraUsuario - UsuarioView");
        this.usuarioSelected = usuario;

        // buscar bitacora de usuario
        this.bitacoraUsuarioList = bitacoraUsuarioService.findByUsuarioId(usuario.getIdUsuario())
                .stream().sorted(Comparator.comparing(BitacoraUsuario::getFechaModificacion))
                .toList();

        this.showPanelPrincipal = false;
        this.showPanelBitacoraUsuario = true;
    }

    public void regresar() {
        log.info("regresar - UsuarioView");
        this.usuarioSelected = null;
        this.showConfigurarPermisos = false;
        this.showPanelPrincipal = true;
        this.limpiarFiltrosPermisos();
    }

    public void buscarPermisos() {
        log.info("buscarPermisos - UsuarioView");
        this.usuarioPermisoList = usuarioPermisoService.findByUsuario(this.usuarioSelected);
    }

    public void limpiarFiltrosPermisos() {
        log.info("limpiarFiltrosPermisos - UsuarioView");
        this.permisoFilter = new Permiso();
        this.usuarioPermisoList = new ArrayList<>();
    }

    @ConfigPermiso(tipo = TipoPermiso.WRITE, codigo = "SEGURIDAD_USUARIOS_WRITE_ASIGNAR_PERMISOS",
            nombre = "Asignar permiso", descripcion = "Acción que permite habilitar, deshabilitar un permiso al usuario seleccionado.")
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_ASIGNAR_PERMISOS')")
    public void asignarPermiso(UsuarioPermiso usuarioPermiso) {
        log.info("asignarPermiso - UsuarioView");
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
    @PreAuthorize("hasAnyAuthority('ROLE_OWNER', 'SEGURIDAD_USUARIOS_WRITE_ACTUALIZAR_PERMISOS')")
    public void actualizarPermisos() {
        log.info("actualizarPermisos - UsuarioView");
        List<Permiso> permisos = permisoScannerService.getPermisos("gob.yucatan.sicasy",
                userSessionBean.getUserName());
        permisoService.updateAll(permisos);
        log.info("Permisos: {}", permisos.size());
        this.buscarPermisos();
        Messages.addInfo("Se ha actualizado el listado de permisos.");
    }
}
