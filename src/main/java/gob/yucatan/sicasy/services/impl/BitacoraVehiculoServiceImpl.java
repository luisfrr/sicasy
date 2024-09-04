package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBitacoraVehiculoRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraVehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitacoraVehiculoServiceImpl implements IBitacoraVehiculoService {

    private final IBitacoraVehiculoRepository bitacoraVehiculoRepository;

    @Override
    public List<BitacoraVehiculo> findAllDynamic(BitacoraVehiculo bitacoraVehiculo) {

        SearchSpecification<BitacoraVehiculo> specification = new SearchSpecification<>();

        if(bitacoraVehiculo.getVehiculo() != null && bitacoraVehiculo.getVehiculo().getIdVehiculo() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraVehiculo.getVehiculo().getIdVehiculo(),
                    BitacoraVehiculo_.VEHICULO, Vehiculo_.ID_VEHICULO));
        }

        return bitacoraVehiculoRepository.findAll(specification);
    }

    @Override
    public List<BitacoraVehiculo> findByVehiculoId(Long vehiculoId) {

        BitacoraVehiculo bitacoraVehiculo = BitacoraVehiculo.builder()
                .vehiculo(Vehiculo.builder().idVehiculo(vehiculoId).build())
                .build();

        return this.findAllDynamic(bitacoraVehiculo);
    }

    @Override
    public void save(BitacoraVehiculo bitacoraVehiculo) {
        bitacoraVehiculoRepository.save(bitacoraVehiculo);
    }

    @Override
    public void saveAll(List<BitacoraVehiculo> bitacoraVehiculo) {
        bitacoraVehiculoRepository.saveAll(bitacoraVehiculo);
    }

    @Override
    public BitacoraVehiculo getBitacoraVehiculo(String accion, Vehiculo vehiculoAnterior, Vehiculo vehiculoNuevo, String username) {

        BitacoraVehiculo bitacoraVehiculo = BitacoraVehiculo.builder()
                .accion(accion)
                .vehiculo(vehiculoNuevo)
                .cambioSet(new HashSet<>())
                .fechaModificacion(new Date())
                .modificadoPor(username)
                .build();
        
        // Resultado final
        List<BitacoraVehiculoCambio> bitacoraVehiculoCambios = new ArrayList<>();

        // Obtener la clase de la entidad Vehículo
        Class<?> vehiculoClass = Vehiculo.class;

        // Obtener todos los campos declarados en la clase Vehículo
        Field[] campos = vehiculoClass.getDeclaredFields();

        List<String> camposToAudit = List.of(Vehiculo_.ID_VEHICULO, Vehiculo_.NO_SERIE, Vehiculo_.PLACA,
                Vehiculo_.MARCA, Vehiculo_.MODELO, Vehiculo_.ANIO, Vehiculo_.NO_MOTOR, Vehiculo_.COLOR,
                Vehiculo_.DESCRIPCION_VEHICULO, Vehiculo_.MONTO_FACTURA, Vehiculo_.NO_FACTURA, Vehiculo_.RENTA_MENSUAL,
                Vehiculo_.PROVEEDOR, Vehiculo_.RESGUARDANTE, Vehiculo_.AREA_RESGUARDANTE,
                Vehiculo_.AUTORIZA_DIRECTOR_ADMIN, Vehiculo_.AUTORIZA_DIRECTOR_GENERAL,
                Vehiculo_.OBSERVACIONES, Vehiculo_.ESTATUS_REGISTRO
        );

        try {
            // Sí se está registrando un nuevo rol
            if (vehiculoAnterior == null) {
                // Iterar sobre cada campo
                for (Field campo : campos) {

                    String nombreCampo = campo.getName();

                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol nuevo
                        Object valorNuevo = campo.get(vehiculoNuevo);
                        String tipoDato = valorNuevo != null ? valorNuevo.getClass().getSimpleName() : "";
                        // Agregar un registro a la bitácora con el campo y el valor nuevo
                        bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null,
                                valorNuevo != null ? valorNuevo.toString() : ""));
                    }

                    if(campo.getName().equals(Vehiculo_.LICITACION)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getLicitacion() != null && vehiculoNuevo.getLicitacion().getIdLicitacion() != null) {
                            String tipoDato = vehiculoNuevo.getLicitacion().getIdLicitacion().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getLicitacion().getIdLicitacion().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.ANEXO)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getAnexo() != null && vehiculoNuevo.getAnexo().getIdAnexo() != null) {
                            String tipoDato = vehiculoNuevo.getAnexo().getIdAnexo().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getAnexo().getIdAnexo().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.DEPENDENCIA)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getDependencia() != null && vehiculoNuevo.getDependencia().getIdDependencia() != null) {
                            String tipoDato = vehiculoNuevo.getDependencia().getIdDependencia().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getDependencia().getIdDependencia().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.DEPENDENCIA_ASIGNADA)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getDependenciaAsignada() != null && vehiculoNuevo.getDependenciaAsignada().getIdDependencia() != null) {
                            String tipoDato = vehiculoNuevo.getDependenciaAsignada().getIdDependencia().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getDependenciaAsignada().getIdDependencia().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.ESTATUS_VEHICULO)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getEstatusVehiculo() != null && vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo() != null) {
                            String tipoDato = vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.CONDICION_VEHICULO)) {
                        if(vehiculoNuevo != null && vehiculoNuevo.getEstatusVehiculo() != null && vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo() != null) {
                            String tipoDato = vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo().getClass().getSimpleName();
                            String valorNuevo = vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo().toString();
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, null, valorNuevo));
                        }
                    }
                }
            } else {
                // Obtener la lista de cambios entre ambas entidades
                for (Field campo : campos) {

                    String nombreCampo = campo.getName();

                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol anterior y el rol nuevo
                        Object valorAnterior = campo.get(vehiculoAnterior);
                        Object valorNuevo = campo.get(vehiculoNuevo);
                        // Comparar los valores y agregar un registro a la bitácora si hay cambios
                        if (!Objects.equals(valorAnterior, valorNuevo)) {
                            //String tipoDato = valorNuevo != null ? valorNuevo.getClass().getSimpleName() : "";
                            String tipoDato = valorNuevo.getClass().getSimpleName();
                            String datoAnterior = valorAnterior != null ? valorAnterior.toString() : null;
                            // Agregar un registro a la bitácora con el campo y el valor nuevo
                            bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnterior, valorNuevo.toString()));
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.LICITACION)) {
                        if(vehiculoAnterior.getLicitacion() != null && vehiculoNuevo.getLicitacion() != null) {
                            Integer datoAnterior = vehiculoAnterior.getLicitacion().getIdLicitacion();
                            Integer datoNuevo = vehiculoNuevo.getLicitacion().getIdLicitacion();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.ANEXO)) {
                        if(vehiculoAnterior.getAnexo() != null && vehiculoNuevo.getAnexo() != null) {
                            Long datoAnterior = vehiculoAnterior.getAnexo().getIdAnexo();
                            Long datoNuevo = vehiculoNuevo.getAnexo().getIdAnexo();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.DEPENDENCIA)) {
                        if(vehiculoAnterior.getDependencia() != null && vehiculoNuevo.getDependencia() != null) {
                            Integer datoAnterior = vehiculoAnterior.getDependencia().getIdDependencia();
                            Integer datoNuevo = vehiculoNuevo.getDependencia().getIdDependencia();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.DEPENDENCIA_ASIGNADA)) {
                        if(vehiculoAnterior.getDependenciaAsignada() != null && vehiculoNuevo.getDependenciaAsignada() != null) {
                            Integer datoAnterior = vehiculoAnterior.getDependenciaAsignada().getIdDependencia();
                            Integer datoNuevo = vehiculoNuevo.getDependenciaAsignada().getIdDependencia();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.ESTATUS_VEHICULO)) {
                        if(vehiculoAnterior.getEstatusVehiculo() != null && vehiculoNuevo.getEstatusVehiculo() != null) {
                            Integer datoAnterior = vehiculoAnterior.getEstatusVehiculo().getIdEstatusVehiculo();
                            Integer datoNuevo = vehiculoNuevo.getEstatusVehiculo().getIdEstatusVehiculo();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                    if(campo.getName().equals(Vehiculo_.CONDICION_VEHICULO)) {
                        if(vehiculoAnterior.getCondicionVehiculo() != null && vehiculoNuevo.getCondicionVehiculo() != null) {
                            Integer datoAnterior = vehiculoAnterior.getCondicionVehiculo().getIdCondicionVehiculo();
                            Integer datoNuevo = vehiculoNuevo.getCondicionVehiculo().getIdCondicionVehiculo();
                            if (!Objects.equals(datoAnterior, datoNuevo)) {
                                String tipoDato = datoNuevo.getClass().getSimpleName();
                                String datoAnteriorStr = datoAnterior != null ? datoAnterior.toString() : null;
                                // Agregar un registro a la bitácora con el campo y el valor nuevo
                                bitacoraVehiculoCambios.add(this.getBitacoraCambio(bitacoraVehiculo, nombreCampo, tipoDato, datoAnteriorStr, datoNuevo.toString()));
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("Error al generar Bitacora Vehiculo Cambios: {}", e.getMessage());
        }
        
        bitacoraVehiculo.setCambioSet(new HashSet<>(bitacoraVehiculoCambios));

        // se devuelve el objeto
        return bitacoraVehiculo;
    }

    private BitacoraVehiculoCambio getBitacoraCambio(BitacoraVehiculo bitacoraVehiculo, String campo, String tipoDato, Object valorAnterior, Object valorNuevo) {
        return BitacoraVehiculoCambio.builder()
                .bitacoraVehiculo(bitacoraVehiculo)
                .campo(campo)
                .tipoDato(tipoDato)
                .valorAnterior(valorAnterior != null ? valorAnterior.toString() : "")
                .valorNuevo(valorNuevo.toString())
                .fechaModificacion(new Date())
                .build();
    }
}
