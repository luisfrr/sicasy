package gob.yucatan.sicasy.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBitacoraRolRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraRolService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitacoraRolSeviceImpl implements IBitacoraRolService {

    private final IBitacoraRolRepository bitacoraRolRepository;

    @Override
    public List<BitacoraRol> findAllDynamic(BitacoraRol bitacoraRol) {

        SearchSpecification<BitacoraRol> specification = new SearchSpecification<>();

        if(bitacoraRol.getRol() != null && bitacoraRol.getRol().getIdRol() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraRol.getRol().getIdRol(),
                    BitacoraRol_.ROL, Rol_.ID_ROL));

        return bitacoraRolRepository.findAll(specification);
    }

    @Override
    public List<BitacoraRol> findByRolId(Long rolId) {

        BitacoraRol bitacoraRol = BitacoraRol.builder()
                .rol(Rol.builder().idRol(rolId).build())
                .build();

        return this.findAllDynamic(bitacoraRol);
    }

    @Override
    public void save(BitacoraRol bitacoraRol) {
        bitacoraRolRepository.save(bitacoraRol);
    }


    @Override
    @Transactional
    public void guardarBitacora(String accion, Rol rolAnterior, Rol rolNuevo, String userName) {

        // Resultado final
        List<BitacoraCambios> bitacoraCambiosList = new ArrayList<>();

        // Obtener la clase de la entidad Rol
        Class<?> rolClass = Rol.class;

        // Obtener todos los campos declarados en la clase Rol
        Field[] campos = rolClass.getDeclaredFields();

        List<String> camposToAudit = List.of(Rol_.ID_ROL, Rol_.NOMBRE, Rol_.CODIGO, Rol_.DESCRIPCION, Rol_.ESTATUS);

        try {

            // Sí se está registrando un nuevo rol
            if (rolAnterior == null) {

                // Iterar sobre cada campo
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol nuevo
                        Object valorNuevo = campo.get(rolNuevo);

                        // Agregar un registro a la bitácora con el campo y el valor nuevo
                        bitacoraCambiosList.add(BitacoraCambios.builder()
                                .campo(campo.getName())
                                .valorAnterior(null) // No hay valor anterior en un registro nuevo
                                .valorNuevo(valorNuevo != null ? valorNuevo.toString() : null)
                                .build());
                    }
                }
            } else {
                // Obtener la lista de cambios entre ambas entidades
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol anterior y el rol nuevo
                        Object valorAnterior = campo.get(rolAnterior);
                        Object valorNuevo = campo.get(rolNuevo);

                        // Comparar los valores y agregar un registro a la bitácora si hay cambios
                        if (!Objects.equals(valorAnterior, valorNuevo)) {
                            bitacoraCambiosList.add(BitacoraCambios.builder()
                                    .campo(campo.getName())
                                    .valorAnterior(valorAnterior != null ? valorAnterior.toString() : null)
                                    .valorNuevo(valorNuevo != null ? valorNuevo.toString() : null)
                                    .build());
                        }
                    }
                }
            }

        } catch (IllegalAccessException e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("Error al guardar BitacoraCambios: {}", e.getMessage());
        }

        // Convertir bitacoraCambiosList a Text Json
        ObjectMapper objectMapper = new ObjectMapper();
        String cambiosJson = "";
        try {
            cambiosJson = objectMapper.writeValueAsString(bitacoraCambiosList);
        } catch (JsonProcessingException e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("No se pudo convertir a JSON String", e);
        }

        // Guardar la información en la tabla bitacora_rol
        // Código para guardar la información omitido por brevedad

        this.save(BitacoraRol.builder()
                        .accion(accion)
                        .rol(rolNuevo)
                        .cambios(cambiosJson)
                        .fechaModificacion(new Date())
                        .modificadoPor(userName)
                .build());
    }

    @Override
    @Transactional
    public void guardarBitacoraPermisos(Rol rol, List<RolPermiso> rolPermisoAnteriorList,
                                        List<RolPermiso> rolPermisoNuevoList,
                                        String userName) {

        // Resultado final
        List<BitacoraCambios> bitacoraCambiosList = new ArrayList<>();

        // Sí se está registrando un nuevo rol
        if (rolPermisoAnteriorList == null || rolPermisoAnteriorList.isEmpty()) {

            for (RolPermiso rolPermiso : rolPermisoNuevoList) {
                // Agregar un registro a la bitácora con el campo y el valor nuevo
                bitacoraCambiosList.add(BitacoraCambios.builder()
                        .campo(rolPermiso.getPermiso().getNombre())
                        .valorAnterior(null) // No hay valor anterior en un registro nuevo
                        .valorNuevo(rolPermiso.getEstatusPermiso() != null ? rolPermiso.getEstatusPermiso().getLabel() : null)
                        .build());
            }

        } else {

            for (RolPermiso rolPermiso : rolPermisoNuevoList) {

                RolPermiso rolPermisoAnterior = rolPermisoAnteriorList.stream()
                        .filter(r -> Objects.equals(r.getIdRolPermiso(), rolPermiso.getIdRolPermiso()))
                        .findFirst().orElse(null);

                if(rolPermisoAnterior != null && rolPermiso.getEstatusPermiso() != null &&
                        !Objects.equals(rolPermisoAnterior.getEstatusPermiso().getLabel(), rolPermiso.getEstatusPermiso().getLabel())) {
                    // Agregar un registro a la bitácora con el campo y el valor nuevo
                    bitacoraCambiosList.add(BitacoraCambios.builder()
                            .campo(rolPermiso.getPermiso().getNombre())
                            .valorAnterior(rolPermisoAnterior.getEstatusPermiso() != null ? rolPermisoAnterior.getEstatusPermiso().getLabel() : null)
                            .valorNuevo(rolPermiso.getEstatusPermiso() != null ? rolPermiso.getEstatusPermiso().getLabel() : null)
                            .build());
                }
            }
        }

        // Convertir bitacoraCambiosList a Text Json
        ObjectMapper objectMapper = new ObjectMapper();
        String cambiosJson = "";
        try {
            cambiosJson = objectMapper.writeValueAsString(bitacoraCambiosList);
        } catch (JsonProcessingException e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("No se pudo convertir a JSON String", e);
        }

        // Guardar la información en la tabla bitacora_rol
        this.save(BitacoraRol.builder()
                .accion("Configuracion de permisos")
                .rol(rol)
                .cambios(cambiosJson)
                .fechaModificacion(new Date())
                .modificadoPor(userName)
                .build());
    }

}
