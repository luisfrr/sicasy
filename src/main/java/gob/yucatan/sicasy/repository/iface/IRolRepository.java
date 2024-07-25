package gob.yucatan.sicasy.repository.iface;

import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IRolRepository extends JpaRepository<Rol, Long>, JpaSpecificationExecutor<Rol> {

    @Query("select count(distinct usuarioRolSet.idUsuarioRol) from Rol r inner join r.usuarioRolSet usuarioRolSet where usuarioRolSet.rol.idRol = ?1")
    long countUsuariosByRolId(Long idRol);

    @Query("select (count(r) > 0) from Rol r where r.nombre = :nombre and r.estatus = :estatus")
    boolean existsByNombreAndEstatus(@Param("nombre") String nombre, @Param("estatus") EstatusRegistro estatus);

    @Query("select (count(r) > 0) from Rol r where r.nombre = :nombre and r.estatus = :estatus and r.idRol <> :idRol")
    boolean existsByNombreAndEstatusAndIdRolNot(@Param("nombre") String nombre, @Param("estatus") EstatusRegistro estatus, @Param("idRol") Long idRol);

}
