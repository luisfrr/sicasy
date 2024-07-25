package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Permiso;
import gob.yucatan.sicasy.business.entities.Permiso_;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IPermisoRepository;
import gob.yucatan.sicasy.services.iface.IPermisoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermisoServiceImpl implements IPermisoService {

    private final IPermisoRepository permisoRepository;

    @Override
    public List<Permiso> findAllDynamic(Permiso permiso) {
        SearchSpecification<Permiso> specification = new SearchSpecification<>();

        if(permiso.getNombre() != null && !permiso.getNombre().isEmpty()){
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    permiso.getNombre(),
                    Permiso_.NOMBRE));
        }

        if(permiso.getEstatus() != null){
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    permiso.getEstatus(),
                    Permiso_.ESTATUS));
        }

        return permisoRepository.findAll(specification);
    }

    @Override
    public List<Permiso> findAll() {
        return permisoRepository.findAll();
    }

    @Override
    public void updateAll(List<Permiso> permisos) {

        List<Permiso> permisoDbList = permisoRepository.findAll();

        for(Permiso permiso : permisos) {
            permisoDbList.stream().filter(p -> Objects.equals(p.getCodigo(), permiso.getCodigo()))
                    .map(Permiso::getIdPermiso)
                    .findFirst()
                    .ifPresent(permiso::setIdPermiso);

            if(permiso.getSubPermisos() != null && !permiso.getSubPermisos().isEmpty()){
                for(Permiso subPermiso : permiso.getSubPermisos()){
                    permisoDbList.stream().filter(p -> Objects.equals(p.getCodigo(), subPermiso.getCodigo()))
                            .map(Permiso::getIdPermiso)
                            .findFirst()
                            .ifPresent(subPermiso::setIdPermiso);
                }
            }
        }

        permisoRepository.saveAll(permisos);
    }
}
