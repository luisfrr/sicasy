package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    @Query("select u from Usuario u left join fetch u.usuarioRolSet where upper(u.usuario) = upper(?1)")
    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

}
