package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.Rol_;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchFetch;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IRolRepository;
import gob.yucatan.sicasy.services.iface.IRolService;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolServiceImpl implements IRolService {

    private final IRolRepository rolRepository;

    @Override
    public List<Rol> findAllDynamic(Rol rol) {

        SearchSpecification<Rol> specification = new SearchSpecification<>();

        if(rol.getNombre() != null && !rol.getNombre().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    rol.getNombre(),
                    Rol_.NOMBRE));

        if(rol.getEstatus() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    rol.getEstatus(),
                    Rol_.ESTATUS));

        if(rol.isLeftJoinUsuarioRolSet())
            specification.add(new SearchFetch(JoinType.LEFT,
                    Rol_.USUARIO_ROL_SET));

        return rolRepository.findAll(specification);
    }

    @Override
    public Optional<Rol> findById(Long id) {
        return rolRepository.findById(id);
    }

    @Override
    public void save(Rol rol) {
        rol.setEstatus(EstatusRegistro.ACTIVO);
        rol.setFechaCreacion(new Date());
        rolRepository.save(rol);
    }

    @Override
    public void delete(Rol rol) {
        Long ID_ROL_OWNER = 1L;
        if(Objects.equals(rol.getIdRol(), ID_ROL_OWNER))
            throw new BadRequestException("No es posible eliminar el rol propietario.");

        Optional<Rol> rolOptional = rolRepository.findById(rol.getIdRol());

        if(rolOptional.isEmpty())
            throw new NotFoundException("Rol no encontrado");

        long usuariosByRol = rolRepository.countUsuariosByRolId(rol.getIdRol());
        if(usuariosByRol > 0)
            throw new BadRequestException("No se puede borrar un rol que tiene usuarios");

        Rol rolToUpdate = rolOptional.get();
        rolToUpdate.setEstatus(EstatusRegistro.BORRADO);
        rolToUpdate.setFechaBorrado(new Date());
        rolToUpdate.setBorradoPor(rol.getBorradoPor());
        rolRepository.save(rolToUpdate);
    }

    @Override
    public void update(Rol rol) {
        Long ID_ROL_OWNER = 1L;
        if(Objects.equals(rol.getIdRol(), ID_ROL_OWNER))
            throw new BadRequestException("No es posible actualizar el rol propietario.");

        Optional<Rol> rolOptional = rolRepository.findById(rol.getIdRol());

        if(rolOptional.isEmpty())
            throw new NotFoundException("Rol no encontrado");

        Rol rolToUpdate = rolOptional.get();
        rolToUpdate.setCodigo(rol.getCodigo());
        rolToUpdate.setNombre(rol.getNombre());
        rolToUpdate.setDescripcion(rol.getDescripcion());
        rolToUpdate.setFechaModificacion(new Date());
        rolToUpdate.setModificadoPor(rol.getModificadoPor());
        rolRepository.save(rolToUpdate);
    }

}
