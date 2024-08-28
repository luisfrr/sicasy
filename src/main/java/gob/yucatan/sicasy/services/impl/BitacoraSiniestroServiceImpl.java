package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.BitacoraCambios;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBitacoraSiniestroRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraSiniestroService;
import gob.yucatan.sicasy.utils.strings.JsonStringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitacoraSiniestroServiceImpl implements IBitacoraSiniestroService {

    private final IBitacoraSiniestroRepository bitacoraSiniestroRepository;

    @Override
    public List<BitacoraSiniestro> findAllDynamic(BitacoraSiniestro bitacoraSiniestro) {
        SearchSpecification<BitacoraSiniestro> specification = new SearchSpecification<>();

        if(bitacoraSiniestro.getSiniestro() != null && bitacoraSiniestro.getSiniestro().getIdSiniestro() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraSiniestro.getSiniestro().getIdSiniestro(),
                    BitacoraSiniestro_.SINIESTRO, Siniestro_.ID_SINIESTRO));

        return bitacoraSiniestroRepository.findAll(specification);
    }

    @Override
    public List<BitacoraSiniestro> findBySiniestroId(Long siniestroId) {
        BitacoraSiniestro bitacoraSiniestro = BitacoraSiniestro.builder()
                .siniestro(Siniestro.builder().idSiniestro(siniestroId).build())
                .build();
        return this.findAllDynamic(bitacoraSiniestro);
    }

    @Override
    public void save(BitacoraSiniestro bitacoraSiniestro) {
        bitacoraSiniestroRepository.save(bitacoraSiniestro);
    }

    @Override
    public void saveAll(List<BitacoraSiniestro> bitacoraSiniestroList) {
        bitacoraSiniestroRepository.saveAll(bitacoraSiniestroList);
    }

    @Override
    public void guardarBitacora(String accion, Siniestro siniestroAnterior, Siniestro siniestroNuevo, String username) {
        BitacoraSiniestro bitacoraSiniestro = getBitacora(accion, siniestroAnterior, siniestroNuevo, username);
        this.save(bitacoraSiniestro);
    }

    @Override
    public BitacoraSiniestro getBitacora(String accion, Siniestro siniestroAnterior, Siniestro siniestroNuevo, String username) {
        // Resultado final
        List<BitacoraCambios> bitacoraCambiosList = new ArrayList<>();

        // Obtener la clase de la entidad Siniestro
        Class<?> siniestroClass = Siniestro.class;

        // Obtener todos los campos declarados en la clase Siniestro
        Field[] campos = siniestroClass.getDeclaredFields();

        List<String> camposToAudit = List.of(Siniestro_.ID_SINIESTRO,
                Siniestro_.VEHICULO, Siniestro_.INCISO, Siniestro_.NO_SINIESTRO_ASEGURADORA,
                Siniestro_.NO_SINIESTRO_SA_F, Siniestro_.AJUSTADOR_ASEGURADORA,
                Siniestro_.AJUSTADOR_SA_F, Siniestro_.ESTADO, Siniestro_.MUNICIPIO,
                Siniestro_.LOCALIDAD, Siniestro_.ESTATUS_SINIESTRO, Siniestro_.CAUSA,
                Siniestro_.RESPONSABLE, Siniestro_.REPORTA, Siniestro_.CONDUCTOR,
                Siniestro_.FECHA_SINIESTRO, Siniestro_.FECHA_REPORTE, Siniestro_.DECLARACION_SINIESTRO,
                Siniestro_.OBSERVACIONES, Siniestro_.NO_LICENCIA, Siniestro_.TIPO_LICENCIA,
                Siniestro_.VENCIMIENTO_LICENCIA, Siniestro_.DANIO_VIA_PUBLICA, Siniestro_.CORRALON,
                Siniestro_.MULTA_VEHICULO, Siniestro_.COSTO_MULTA, //Siniestro_.DEDUCIBLE,
                Siniestro_.PERDIDA_TOTAL, Siniestro_.ESTATUS_REGISTRO,
                Usuario_.ESTATUS);

        try {

            // Sí se está registrando un nuevo registro
            if (siniestroAnterior == null) {

                // Iterar sobre cada campo
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo
                        Object valorNuevo = switch (campo.getName()) {
                            case Siniestro_.VEHICULO -> siniestroNuevo.getVehiculo().getNoSerie();
                            case Siniestro_.INCISO ->  siniestroNuevo.getInciso() != null ? String.format("Póliza: %s | Inciso: %s",
                                    siniestroNuevo.getInciso().getPoliza().getNumeroPoliza(),
                                    siniestroNuevo.getInciso().getNumeroInciso()) : Strings.EMPTY;
                            case Siniestro_.ESTATUS_SINIESTRO -> siniestroNuevo.getEstatusSiniestro().getNombre();
                            default -> campo.get(siniestroNuevo);
                        };

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
                        Object valorAnterior;
                        Object valorNuevo;

                        switch (campo.getName()) {
                            case Siniestro_.VEHICULO -> {
                                valorAnterior = siniestroAnterior.getVehiculo().getNoSerie();
                                valorNuevo = siniestroNuevo.getVehiculo().getNoSerie();
                            }
                            case Siniestro_.INCISO -> {
                                valorAnterior = siniestroAnterior.getInciso() != null ? String.format("Póliza: %s | Inciso: %s",
                                        siniestroAnterior.getInciso().getPoliza().getNumeroPoliza(),
                                        siniestroAnterior.getInciso().getNumeroInciso()) : Strings.EMPTY;
                                valorNuevo = siniestroNuevo.getInciso() != null ? String.format("Póliza: %s | Inciso: %s",
                                        siniestroNuevo.getInciso().getPoliza().getNumeroPoliza(),
                                        siniestroNuevo.getInciso().getNumeroInciso()) : Strings.EMPTY;
                            }
                            case Siniestro_.ESTATUS_SINIESTRO -> {
                                valorAnterior = siniestroAnterior.getEstatusSiniestro().getNombre();
                                valorNuevo = siniestroNuevo.getEstatusSiniestro().getNombre();
                            }
                            default -> {
                                valorAnterior = campo.get(siniestroAnterior);
                                valorNuevo = campo.get(siniestroNuevo);
                            }
                        }

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
        String cambiosJson = JsonStringConverter.convertToString(bitacoraCambiosList);

        return BitacoraSiniestro.builder()
                .accion(accion)
                .siniestro(siniestroNuevo)
                .cambios(cambiosJson)
                .fechaModificacion(new Date())
                .modificadoPor(username)
                .build();
    }
}
