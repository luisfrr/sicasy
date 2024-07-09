package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Mantenimiento;
import gob.yucatan.sicasy.business.entities.Mantenimiento_;
import gob.yucatan.sicasy.business.entities.Vehiculo;
import gob.yucatan.sicasy.business.entities.Vehiculo_;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IMantenimientoRepository;
import gob.yucatan.sicasy.services.iface.IMantenimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MantenimientoServiceImpl implements IMantenimientoService {

    private final IMantenimientoRepository mantenimientoRepository;

    @Override
    public List<Mantenimiento> findAllDynamic(Mantenimiento mantenimiento) {

        SearchSpecification<Mantenimiento> specification = new SearchSpecification<>();

        if(mantenimiento.getVehiculo() != null && mantenimiento.getVehiculo().getIdVehiculo() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    mantenimiento.getVehiculo().getIdVehiculo(),
                    Mantenimiento_.VEHICULO, Vehiculo_.ID_VEHICULO));
        }

        if(mantenimiento.getEstatus() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    mantenimiento.getEstatus(),
                    Mantenimiento_.ESTATUS));
        }

        return mantenimientoRepository.findAll(specification);
    }

    @Override
    public List<Mantenimiento> findByVehiculoId(Long idVehiculo) {

        Mantenimiento mantenimiento = Mantenimiento.builder()
                .vehiculo(Vehiculo.builder().idVehiculo(idVehiculo).build())
                .estatus(EstatusRegistro.ACTIVO)
                .build();

        return this.findAllDynamic(mantenimiento);
    }

    @Override
    public Mantenimiento findById(Long id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha logrado encontrar la información del vehículo."));
    }

    @Override
    public Mantenimiento save(Mantenimiento mantenimiento) {
        if(mantenimiento.getIdMantenimiento() != null) {
            Mantenimiento mantenimientoToUpdate = this.findById(mantenimiento.getIdMantenimiento());
            //mantenimientoToUpdate.setVehiculo(mantenimiento.getVehiculo()); // Vehiculo no se puede actualizar
            mantenimientoToUpdate.setTipoMantenimiento(mantenimiento.getTipoMantenimiento());
            mantenimientoToUpdate.setDescripcion(mantenimiento.getDescripcion());
            mantenimientoToUpdate.setFechaInicio(mantenimiento.getFechaInicio());
            mantenimientoToUpdate.setFechaInicio(mantenimiento.getFechaFin());
            mantenimientoToUpdate.setTipoMantenimiento(mantenimiento.getTipoMantenimiento());
            mantenimientoToUpdate.setTipoMantenimiento(mantenimiento.getTipoMantenimiento());

            return mantenimientoRepository.save(mantenimiento);
        } else {
            mantenimiento.setFechaCreacion(new Date());
            mantenimiento.setEstatus(EstatusRegistro.ACTIVO);
            return mantenimientoRepository.save(mantenimiento);
        }
    }

    @Override
    public void delete(Long id, String username) {
        Mantenimiento mantenimiento = this.findById(id);
        mantenimiento.setEstatus(EstatusRegistro.BORRADO);
        mantenimiento.setFechaBorrado(new Date());
        mantenimiento.setBorradoPor(username);
        mantenimientoRepository.save(mantenimiento);
    }

}
