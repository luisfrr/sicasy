package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.BitacoraUsuario;
import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.entities.UsuarioPermiso;

import java.util.List;

public interface IBitacoraUsuarioService {

    List<BitacoraUsuario> findAllDynamic(BitacoraUsuario bitacoraUsuario);
    List<BitacoraUsuario> findByUsuarioId(Long usuarioId);
    void save(BitacoraUsuario usuario);
    void guardarBitacora(String accion, Usuario usuarioAnterior, Usuario usuarioNuevo, String username);
    void guardarBitacoraPermisos(Usuario usuario, List<UsuarioPermiso> usuarioPermisoAnteriorList,
                                 List<UsuarioPermiso> usuarioPermisoNuevoList, String userName);


}
