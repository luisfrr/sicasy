package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusPermiso;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IRolPermisoRepository;
import gob.yucatan.sicasy.repository.iface.IUsuarioPermisoRepository;
import gob.yucatan.sicasy.repository.iface.IUsuarioRepository;
import gob.yucatan.sicasy.services.iface.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioPermisoServiceImpl implements IUsuarioPermisoService {

    private final IUsuarioPermisoRepository usuarioPermisoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IRolPermisoRepository rolPermisoRepository;
    private final IPermisoService permisoService;
    private final IBitacoraUsuarioService bitacoraUsuarioService;

    @Override
    public List<UsuarioPermiso> findAllDynamic(UsuarioPermiso usuarioPermiso) {

        SearchSpecification<UsuarioPermiso> specification = new SearchSpecification<>();

        if(usuarioPermiso.getUsuario() != null && usuarioPermiso.getUsuario().getIdUsuario() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    usuarioPermiso.getUsuario().getIdUsuario(),
                    UsuarioPermiso_.USUARIO, Usuario_.ID_USUARIO));

        if(usuarioPermiso.getPermisoParentId() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    usuarioPermiso.getPermisoParentId(),
                    UsuarioPermiso_.PERMISO, Permiso_.PERMISO_PARENT, Permiso_.ID_PERMISO));

        if(usuarioPermiso.getEstatusPermiso() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    usuarioPermiso.getEstatusPermiso(),
                    UsuarioPermiso_.ESTATUS_PERMISO));

        return usuarioPermisoRepository.findAll(specification);
    }

    @Override
    public List<UsuarioPermiso> findByUsuario(Usuario usuario, Permiso permisoFilter) {

        // Se buscan todos los permisos activos de la aplicacion
        List<Permiso> permisoList = permisoService.findAllDynamic(Permiso.builder()
                .estatus(EstatusRegistro.ACTIVO)
                .nombre(permisoFilter.getNombre())
                .build());

        // Se buscan todos los permisos que tiene asignado el usuario
        List<UsuarioPermiso> usuarioPermisoList = this.findAllDynamic(UsuarioPermiso.builder().usuario(usuario).build());

        // Se obtiene una lista de los permisos que no han sido registrados
        List<Permiso> permisosNoRegistrados = permisoList.stream()
                .filter(permiso -> usuarioPermisoList.stream()
                        .noneMatch(usuarioPermiso -> usuarioPermiso.getPermiso().equals(permiso)))
                .toList();

        // Se agregan todos los permisos no registrados en la lista RolPermiso
        for (Permiso permisoNoRegistrado : permisosNoRegistrados) {
            usuarioPermisoList.add(UsuarioPermiso.builder()
                    .usuario(usuario)
                    .permiso(permisoNoRegistrado)
                    .estatusPermiso(EstatusPermiso.SIN_ASIGNAR)
                    .build());
        }

        List<UsuarioPermiso> usuarioPermisoParents = usuarioPermisoList.stream()
                .filter(usuarioPermiso -> usuarioPermiso.getPermiso().getPermisoParent() == null)
                .sorted(Comparator.comparing(r -> r.getPermiso().getNombre()))
                .toList();
        for (UsuarioPermiso usuarioPermisoParent : usuarioPermisoParents) {
            List<UsuarioPermiso> subUsuarioPermisoList = usuarioPermisoList.stream()
                    .filter(up -> Objects.equals(up.getPermiso().getPermisoParent(), usuarioPermisoParent.getPermiso()))
                    .sorted(Comparator.comparing((UsuarioPermiso r) -> r.getPermiso().getTipoPermiso())
                            .thenComparing((UsuarioPermiso r) -> r.getPermiso().getOrden()))
                    .toList();

            usuarioPermisoParent.setSubUsuarioPermisoList(subUsuarioPermisoList);
        }

        return usuarioPermisoParents;
    }

    @Override
    public void asignarPermiso(UsuarioPermiso usuarioPermiso, String userName) {

        Long id = usuarioPermiso.getIdUsuarioPermiso() != null ? usuarioPermiso.getIdUsuarioPermiso() : 0;
        Optional<UsuarioPermiso> usuarioPermisoOptional = this.usuarioPermisoRepository.findById(id);

        // Si es un permiso padre
        if(usuarioPermiso.getPermiso().getPermisoParent() == null) {

            // Y si su estatus cambio a SIN_ASIGNAR o DESHABILITADO
            if(usuarioPermiso.getEstatusPermiso() == EstatusPermiso.SIN_ASIGNAR ||
                    usuarioPermiso.getEstatusPermiso() == EstatusPermiso.DESHABILITADO) {

                List<UsuarioPermiso> usuarioPermisoAnteriorList = new ArrayList<>();
                usuarioPermisoOptional.ifPresent(usuarioPermisoAnteriorList::add);

                // Se buscan los subPermisos de ese permiso y es rol
                List<UsuarioPermiso> subPermisos = findAllDynamic(UsuarioPermiso.builder()
                        .usuario(usuarioPermiso.getUsuario())
                        .permisoParentId(usuarioPermiso.getPermiso().getIdPermiso())
                        .build());

                List<UsuarioPermiso> subPermisosToUpdate = findAllDynamic(UsuarioPermiso.builder()
                        .usuario(usuarioPermiso.getUsuario())
                        .permisoParentId(usuarioPermiso.getPermiso().getIdPermiso())
                        .build());

                // Se agregan los subPermisos
                usuarioPermisoAnteriorList.addAll(subPermisos);

                // Y se marcan en SIN_ASIGNAR
                subPermisosToUpdate.forEach(subPermiso -> subPermiso.setEstatusPermiso(EstatusPermiso.SIN_ASIGNAR));

                List<UsuarioPermiso> usuarioPermisoNuevoList = new ArrayList<>();
                usuarioPermisoNuevoList.add(usuarioPermiso);
                usuarioPermisoNuevoList.addAll(subPermisosToUpdate);

                // Se guarda la bitacora
                bitacoraUsuarioService.guardarBitacoraPermisos(usuarioPermiso.getUsuario(), usuarioPermisoAnteriorList,
                        usuarioPermisoNuevoList, userName);

                // Se actualiza el permiso principal
                usuarioPermisoRepository.save(usuarioPermiso);

                // Se actualizan los subPermisos en la DB
                usuarioPermisoRepository.saveAll(subPermisosToUpdate);
            } else {

                List<UsuarioPermiso> usuarioPermisoAnteriorList = new ArrayList<>();
                usuarioPermisoOptional.ifPresent(usuarioPermisoAnteriorList::add);

                List<UsuarioPermiso> usuarioPermisoNuevoList = List.of(usuarioPermiso);

                // Se guarda la bitacora
                bitacoraUsuarioService.guardarBitacoraPermisos(usuarioPermiso.getUsuario(), usuarioPermisoAnteriorList,
                        usuarioPermisoNuevoList, userName);

                usuarioPermisoRepository.save(usuarioPermiso);

            }
        } else {

            List<UsuarioPermiso> usuarioPermisoAnteriorList = new ArrayList<>();
            usuarioPermisoOptional.ifPresent(usuarioPermisoAnteriorList::add);

            List<UsuarioPermiso> usuarioPermisoNuevoList = List.of(usuarioPermiso);

            // Se guarda la bitacora
            bitacoraUsuarioService.guardarBitacoraPermisos(usuarioPermiso.getUsuario(), usuarioPermisoAnteriorList,
                    usuarioPermisoNuevoList, userName);

            usuarioPermisoRepository.save(usuarioPermiso);
        }

    }

    @Override
    public List<? extends GrantedAuthority> getAuthorities(Long idUsuario) {

        // Se busca la información del usuario
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);
        if(usuarioOptional.isEmpty())
            throw new NotFoundException("No se ha encontrado el usuario");

        // Se obtiene la información del usuario
        Usuario usuario = usuarioOptional.get();

        // Se obtienen los roles del usuario
        List<Rol> rolList = usuario.getUsuarioRolSet().stream()
                .map(UsuarioRol::getRol)
                .toList();

        // Se obtienen todos los permisos habilitados por rol
        List<Permiso> permisoRolList = rolPermisoRepository
                .findPermisoByRolInAndEstatusPermiso(rolList, EstatusPermiso.HABILITADO);

        // Se obtiene todos los permisos deshabilitados por usuario
        List<Permiso> permisoDeshabilitadoUsuarioList = usuarioPermisoRepository
                .findPermisoByUsuarioAndEstatusPermiso(usuario, EstatusPermiso.DESHABILITADO);

        // Se quitan los permisos deshabilitados en caso de que esten habilitado por los permisos de roles
        permisoRolList.removeAll(permisoDeshabilitadoUsuarioList);

        // Se obtiene todos los permisos habilitados por usuario
        List<Permiso> permisoHabilitadoUsuarioList = usuarioPermisoRepository
                .findPermisoByUsuarioAndEstatusPermiso(usuario, EstatusPermiso.HABILITADO);

        // Se obtiene una lista unica de permisos habilitados
        Set<String> codigoPermisoHabilitado = new HashSet<>();
        codigoPermisoHabilitado.addAll(permisoRolList.stream().map(Permiso::getCodigo).toList());
        codigoPermisoHabilitado.addAll(permisoHabilitadoUsuarioList.stream().map(Permiso::getCodigo).toList());

        // Se agrega a la lista final
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        rolList.forEach(rol -> grantedAuthorities.add(new SimpleGrantedAuthority(rol.getCodigo())));
        codigoPermisoHabilitado.forEach(codigo -> grantedAuthorities.add(new SimpleGrantedAuthority(codigo)));

        return grantedAuthorities;
    }
}
