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
    public BitacoraVehiculo guardarBitacora(String accion, Vehiculo vehiculoAnterior, Vehiculo vehiculoNuevo, String username) {

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
                Vehiculo_.OBSERVACIONES
                //,Vehiculo_.LICITACION, Vehiculo_.ANEXO,
                //Vehiculo_.DEPENDENCIA, Vehiculo_.DEPENDENCIA_ASIGNADA,
                //Vehiculo_.ESTATUS_VEHICULO, Vehiculo_.CONDICION_VEHICULO
        );

        try {
            // Sí se está registrando un nuevo rol
            if (vehiculoAnterior == null) {
                // Iterar sobre cada campo
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol nuevo
                        Object valorNuevo = campo.get(vehiculoNuevo);
                        // Agregar un registro a la bitácora con el campo y el valor nuevo
                        bitacoraVehiculoCambios.add(BitacoraVehiculoCambio.builder()
                                .campo(campo.getName())
                                .tipoDato(valorNuevo.getClass().getName())
                                .valorAnterior(null) // No hay valor anterior en un registro nuevo
                                .valorNuevo(valorNuevo.toString())
                                .fechaModificacion(new Date())
                                .build());
                    }
                }
            } else {
                // Obtener la lista de cambios entre ambas entidades
                for (Field campo : campos) {
                    if(camposToAudit.contains(campo.getName())) {
                        campo.setAccessible(true); // Permitir acceso a campos privados
                        // Obtener el valor del campo en el rol anterior y el rol nuevo
                        Object valorAnterior = campo.get(vehiculoAnterior);
                        Object valorNuevo = campo.get(vehiculoNuevo);
                        // Comparar los valores y agregar un registro a la bitácora si hay cambios
                        if (!Objects.equals(valorAnterior, valorNuevo)) {
                            bitacoraVehiculoCambios.add(BitacoraVehiculoCambio.builder()
                                    .campo(campo.getName())
                                    .tipoDato(valorNuevo.getClass().getName())
                                    .valorAnterior(valorAnterior != null ? valorAnterior.toString() : null)
                                    .valorNuevo(valorNuevo.toString())
                                    .fechaModificacion(new Date())
                                    .build());
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            // Manejo del error, por ejemplo, loggearlo
            log.error("Error al generar Bitacora Vehiculo Cambios: {}", e.getMessage());
        }

        // se devuelve el objeto
        return BitacoraVehiculo.builder()
                .accion(accion)
                .vehiculo(vehiculoNuevo)
                .cambioSet(new HashSet<>(bitacoraVehiculoCambios))
                .fechaModificacion(new Date())
                .modificadoPor(username)
                .build();
    }

    private BitacoraVehiculoCambio getBitacoraCambio(String campo, String tipoDato, Object valorAnterior, Object valorNuevo) {
        return BitacoraVehiculoCambio.builder()
                .campo(campo)
                .tipoDato(tipoDato)
                .valorAnterior(valorAnterior != null ? valorAnterior.toString() : "")
                .valorNuevo(valorNuevo.toString())
                .fechaModificacion(new Date())
                .build();
    }
}
