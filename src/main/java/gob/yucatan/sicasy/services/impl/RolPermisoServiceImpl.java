package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IRolPermisoRepository;
import gob.yucatan.sicasy.services.iface.IPermisoService;
import gob.yucatan.sicasy.services.iface.IRolPermisoService;
import jakarta.persistence.Transient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RolPermisoServiceImpl implements IRolPermisoService {

    private final IRolPermisoRepository rolPermisoRepository;
    private final IPermisoService permisoService;

    @Override
    public List<RolPermiso> findAllDynamic(RolPermiso rolPermiso) {

        SearchSpecification<RolPermiso> specification = new SearchSpecification<>();

        if(rolPermiso.getRol() != null && rolPermiso.getRol().getIdRol() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    rolPermiso.getRol().getIdRol(),
                    RolPermiso_.ROL, Rol_.ID_ROL));

        if(rolPermiso.getPermisoParentId() != null)
            specification.add(new SearchCriteria(SearchOperation.IN,
                    rolPermiso.getPermisoParentId(),
                    RolPermiso_.PERMISO, Permiso_.PERMISO_PARENT, Permiso_.ID_PERMISO));

        if(rolPermiso.getEstatusPermiso() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    rolPermiso.getEstatusPermiso(),
                    RolPermiso_.ESTATUS_PERMISO));

        return rolPermisoRepository.findAll(specification);
    }

    @Override
    public List<RolPermiso> findByRol(Rol rol) {

        // Se buscan todos los permisos activos de la aplicacion
        List<Permiso> permisoList = permisoService.findAllDynamic(Permiso.builder()
                .estatus(EstatusRegistro.ACTIVO)
                .build());

        // Se buscan todos los permisos que tiene asignado el rol
        List<RolPermiso> rolPermisoList = this.findAllDynamic(RolPermiso.builder().rol(rol).build());

        // Se obtiene una lista de los permisos que no han sido registrados
        List<Permiso> permisosNoRegistrados = permisoList.stream()
                .filter(permiso -> rolPermisoList.stream()
                        .noneMatch(rolPermiso -> rolPermiso.getPermiso().equals(permiso)))
                .toList();

        // Se agregan todos los permisos no registrados en la lista RolPermiso
        for (Permiso permisoNoRegistrado : permisosNoRegistrados) {
            rolPermisoList.add(RolPermiso.builder()
                            .rol(rol)
                            .permiso(permisoNoRegistrado)
                            .estatusPermiso(EstatusPermiso.SIN_ASIGNAR)
                    .build());
        }

        List<RolPermiso> rolPermisoParents = rolPermisoList.stream()
                .filter(rolPermiso -> rolPermiso.getPermiso().getPermisoParent() == null)
                .sorted(Comparator.comparing(r -> r.getPermiso().getNombre()))
                .toList();

        for (RolPermiso rolPermisoParent : rolPermisoParents) {
            List<RolPermiso> subRolPermisoList = rolPermisoList.stream()
                    .filter(rp -> Objects.equals(rp.getPermiso().getPermisoParent(), rolPermisoParent.getPermiso()))
                    .sorted(Comparator.comparing(r -> r.getPermiso().getNombre()))
                    .toList();

            rolPermisoParent.setSubRolPermisoList(subRolPermisoList);
        }

        return rolPermisoParents;
    }

    @Override
    @Transient
    public void asignarPermiso(RolPermiso rolPermiso) {
        // Si es un permiso padre
        if(rolPermiso.getPermiso().getPermisoParent() == null) {
            // Y si su estatus cambio a SIN_ASIGNAR o DESHABILITADO
            if(rolPermiso.getEstatusPermiso() == EstatusPermiso.SIN_ASIGNAR ||
                    rolPermiso.getEstatusPermiso() == EstatusPermiso.DESHABILITADO) {
                // Se buscan los subPermisos de ese permiso y es rol
                List<RolPermiso> subPermisos = findAllDynamic(RolPermiso.builder()
                        .rol(rolPermiso.getRol())
                        .permisoParentId(rolPermiso.getPermiso().getIdPermiso())
                        .build());
                // Y se marcan en SIN_ASIGNAR
                subPermisos.forEach(subPermiso -> subPermiso.setEstatusPermiso(EstatusPermiso.SIN_ASIGNAR));
                // Se actualizan en la DB
                rolPermisoRepository.saveAll(subPermisos);
            }
        }
        rolPermisoRepository.save(rolPermiso);
    }
}
