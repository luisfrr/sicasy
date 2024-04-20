package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    @Query("select u from Usuario u left join fetch u.usuarioRolSet where upper(u.usuario) = upper(?1)")
    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

    @Query("""
            select (count(u) > 0) from Usuario u
            where u.estatus <> ?1 and upper(u.usuario) = upper(?2)""")
    boolean existsByEstatusNotAndUsuario(EstatusUsuario estatus, String usuario);

    @Query("""
            select (count(u) > 0) from Usuario u
            where u.estatus <> ?1 and upper(u.usuario) = upper(?2)""")
    boolean existsByEstatusNotAndEmail(EstatusUsuario estatus, String email);

    @Query("select u from Usuario u where u.token = ?1")
    Optional<Usuario> findByToken(String token);

}
