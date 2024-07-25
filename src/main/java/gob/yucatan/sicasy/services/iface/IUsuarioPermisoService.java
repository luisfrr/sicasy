package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.entities.UsuarioPermiso;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface IUsuarioPermisoService {

    List<UsuarioPermiso> findAllDynamic(UsuarioPermiso usuarioPermiso);
    List<UsuarioPermiso> findByUsuario(Usuario usuario, Permiso permisoFilter);
    void asignarPermiso(UsuarioPermiso usuarioPermiso, String userName);
    List<? extends GrantedAuthority> getAuthorities(Long idUsuario);

}
