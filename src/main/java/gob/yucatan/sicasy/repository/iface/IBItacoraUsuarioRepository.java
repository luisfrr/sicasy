package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.BitacoraUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IBItacoraUsuarioRepository extends JpaRepository<BitacoraUsuario, Long>, JpaSpecificationExecutor<BitacoraUsuario> {
}
