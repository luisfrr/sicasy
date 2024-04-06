package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IUsuarioRolRepository extends JpaRepository<UsuarioRol, Long>, JpaSpecificationExecutor<UsuarioRol> {
}
