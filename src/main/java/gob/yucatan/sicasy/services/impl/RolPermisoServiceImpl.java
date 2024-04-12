package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.RolPermiso;
import gob.yucatan.sicasy.business.entities.RolPermiso_;
import gob.yucatan.sicasy.business.entities.Rol_;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IRolPermisoRepository;
import gob.yucatan.sicasy.services.iface.IRolPermisoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolPermisoServiceImpl implements IRolPermisoService {

    private final IRolPermisoRepository rolPermisoRepository;

    @Override
    public List<RolPermiso> findAllDynamic(RolPermiso rolPermiso) {

        SearchSpecification<RolPermiso> specification = new SearchSpecification<>();

        if(rolPermiso.getRol() != null && rolPermiso.getRol().getIdRol() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    rolPermiso.getRol().getIdRol(),
                    RolPermiso_.ROL, Rol_.ID_ROL));

        if(rolPermiso.getEstatusPermiso() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    rolPermiso.getEstatusPermiso(),
                    RolPermiso_.ESTATUS_PERMISO));

        return rolPermisoRepository.findAll(specification);
    }

    @Override
    public List<RolPermiso> findByRol(Rol rol) {
        RolPermiso rolPermiso = RolPermiso.builder()
                .rol(rol)
                .build();
        return this.findAllDynamic(rolPermiso);
    }
}
