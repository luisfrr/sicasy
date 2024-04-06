package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    List<Usuario> findAllDynamic(Usuario usuario);
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByUsuario(String usuario);

}
