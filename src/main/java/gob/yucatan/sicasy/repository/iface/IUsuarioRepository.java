package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IUsuarioRepository extends JpaRepository<Long, Usuario>, JpaSpecificationExecutor<Usuario> {
}
