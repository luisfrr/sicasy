package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.entities.UsuarioPermiso;
import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IUsuarioPermisoRepository extends JpaRepository<UsuarioPermiso, Long>, JpaSpecificationExecutor<UsuarioPermiso> {

    @Query("select u.permiso from UsuarioPermiso u where u.usuario = ?1 and u.estatusPermiso = ?2")
    List<Permiso> findPermisoByUsuarioAndEstatusPermiso(Usuario usuario, EstatusPermiso estatusPermiso);

}
