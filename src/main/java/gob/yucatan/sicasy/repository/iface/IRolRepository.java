package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface IRolRepository extends JpaRepository<Rol, Long>, JpaSpecificationExecutor<Rol> {

    @Query("select count(distinct usuarioRolSet.idUsuarioRol) from Rol r inner join r.usuarioRolSet usuarioRolSet where usuarioRolSet.rol.idRol = ?1")
    long countUsuariosByRolId(Long idRol);
}
