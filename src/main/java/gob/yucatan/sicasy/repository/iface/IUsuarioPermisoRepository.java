package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.UsuarioPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IUsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, Long>, JpaSpecificationExecutor<UsuarioPermiso> {
}
