package gob.yucatan.sicasy.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBItacoraUsuarioRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraUsuarioService;
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
public class BitacoraUsuarioServiceImpl implements IBitacoraUsuarioService {

    private final IBItacoraUsuarioRepository bitacoraUsuarioRepository;

    @Override
    public List<BitacoraUsuario> findAllDynamic(BitacoraUsuario bitacoraUsuario) {

        SearchSpecification<BitacoraUsuario> specification = new SearchSpecification<>();

        if(bitacoraUsuario.getUsuario() != null && bitacoraUsuario.getUsuario().getIdUsuario() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraUsuario.getUsuario().getIdUsuario(),
                    BitacoraUsuario_.USUARIO, Usuario_.ID_USUARIO));

        return bitacoraUsuarioRepository.findAll(specification);
    }

    @Override
    public List<BitacoraUsuario> findByUsuarioId(Long usuarioId) {

        BitacoraUsuario bitacoraUsuario = BitacoraUsuario.builder()
                .usuario(Usuario.builder().idUsuario(usuarioId).build())
                .build();

        return this.findAllDynamic(bitacoraUsuario);
    }

    @Override
    public void save(BitacoraUsuario usuario) {
        bitacoraUsuarioRepository.save(usuario);
    }

    @Override
    public void guardarBitacora(String accion, Usuario usuarioAnterior, Usuario usuarioNuevo, String username) {
        // Resultado final
        List<BitacoraCambios> bitacoraCambiosList = new ArrayList<>();

        // Obtener la clase de la entidad Rol
        Class<?> usuarioClass = Usuario.class;

        // Obtener todos los campos declarados en la clase Rol
        Field[] campos = usuarioClass.getDeclaredFields();

        List<String> camposToAudit = List.of(Usuario_.ID_USUARIO, Usuario_.USUARIO,
                Usuario_.CONTRASENIA, Usuario_.NOMBRE, Usuario_.EMAIL,
                Usuario_.CORREO_CONFIRMADO, Usuario_.TOKEN,
                Usuario_.TOKEN_TYPE, Usuario_.VIGENCIA_TOKEN,
                Usuario_.ESTATUS);

        try {

            // Sí se está registrando un nuevo rol
            if (usuarioAnterior == null) {

                // Iterar sobre cada campo
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol nuevo
                        Object valorNuevo = campo.get(usuarioNuevo);

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
                        Object valorAnterior = campo.get(usuarioAnterior);
                        Object valorNuevo = campo.get(usuarioNuevo);

                        // Comparar los valores y agregar un registro a la bitácora si hay cambios
                        if (!Objects.equals(valorAnterior, valorNuevo)) {
                            bitacoraCambiosList.add(BitacoraCambios.builder()
                                    .campo(campo.getName())
                                    .valorAnterior(valorAnterior != null ? valorAnterior.toString() : null)
                                    .valorNuevo(valorNuevo != null ? valorNuevo.toString() : null)
                                    .build());
                        }
                    }

                    //
                    if(campo.getName().equals(Usuario_.USUARIO_ROL_SET)) {

                        String rolesAnterior = usuarioAnterior.getRoles();
                        String rolesNuevo = usuarioNuevo.getRoles();

                        // Comparar los valores y agregar un registro a la bitácora si hay cambios
                        if (!Objects.equals(rolesAnterior, rolesNuevo)) {
                            bitacoraCambiosList.add(BitacoraCambios.builder()
                                    .campo(campo.getName())
                                    .valorAnterior(rolesAnterior)
                                    .valorNuevo(rolesNuevo)
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

        this.save(BitacoraUsuario.builder()
                .accion(accion)
                .usuario(usuarioNuevo)
                .cambios(cambiosJson)
                .fechaModificacion(new Date())
                .modificadoPor(username)
                .build());
    }

    @Override
    public void guardarBitacoraPermisos(Usuario usuario, List<UsuarioPermiso> usuarioPermisoAnteriorList,
                                        List<UsuarioPermiso> usuarioPermisoNuevoList, String userName) {
        // Resultado final
        List<BitacoraCambios> bitacoraCambiosList = new ArrayList<>();

        // Sí se está registrando un nuevo rol
        if (usuarioPermisoAnteriorList == null || usuarioPermisoAnteriorList.isEmpty()) {

            for (UsuarioPermiso usuarioPermiso : usuarioPermisoNuevoList) {
                // Agregar un registro a la bitácora con el campo y el valor nuevo
                bitacoraCambiosList.add(BitacoraCambios.builder()
                        .campo(usuarioPermiso.getPermiso().getNombre())
                        .valorAnterior(null) // No hay valor anterior en un registro nuevo
                        .valorNuevo(usuarioPermiso.getEstatusPermiso() != null ? usuarioPermiso.getEstatusPermiso().getLabel() : null)
                        .build());
            }

        } else {

            for (UsuarioPermiso usuarioPermiso : usuarioPermisoNuevoList) {

                UsuarioPermiso usuarioPermisoAnterior = usuarioPermisoAnteriorList.stream()
                        .filter(r -> Objects.equals(r.getIdUsuarioPermiso(), usuarioPermiso.getIdUsuarioPermiso()))
                        .findFirst().orElse(null);

                if(usuarioPermisoAnterior != null && usuarioPermiso.getEstatusPermiso() != null &&
                        !Objects.equals(usuarioPermisoAnterior.getEstatusPermiso().getLabel(), usuarioPermiso.getEstatusPermiso().getLabel())) {
                    // Agregar un registro a la bitácora con el campo y el valor nuevo
                    bitacoraCambiosList.add(BitacoraCambios.builder()
                            .campo(usuarioPermiso.getPermiso().getNombre())
                            .valorAnterior(usuarioPermisoAnterior.getEstatusPermiso() != null ? usuarioPermisoAnterior.getEstatusPermiso().getLabel() : null)
                            .valorNuevo(usuarioPermiso.getEstatusPermiso() != null ? usuarioPermiso.getEstatusPermiso().getLabel() : null)
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
        this.save(BitacoraUsuario.builder()
                .accion("Configuracion de permisos")
                .usuario(usuario)
                .cambios(cambiosJson)
                .fechaModificacion(new Date())
                .modificadoPor(userName)
                .build());

    }
}
