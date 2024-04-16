package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.entities.UsuarioPermiso;

import java.util.List;

public interface IUsuarioPermisoService {

    List<UsuarioPermiso> findAllDynamic(UsuarioPermiso usuarioPermiso);
    List<UsuarioPermiso> findByUsuario(Usuario usuario);
    void asignarPermiso(UsuarioPermiso usuarioPermiso, String userName);

}
