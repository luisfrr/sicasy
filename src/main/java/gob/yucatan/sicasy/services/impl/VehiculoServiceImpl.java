package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IVehiculoRepository;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoServiceImpl implements IVehiculoService {

    private final IVehiculoRepository vehiculoRepository;

    @Override
    public List<Vehiculo> findAllDynamic(Vehiculo vehiculo) {

        SearchSpecification<Vehiculo> specification = new SearchSpecification<>();

        if(vehiculo.getDependencia() != null && vehiculo.getDependencia().getIdDependencia() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getDependencia().getIdDependencia(),
                    Vehiculo_.DEPENDENCIA, Dependencia_.ID_DEPENDENCIA));

        if(vehiculo.getCondicionVehiculo() != null &&
                vehiculo.getCondicionVehiculo().getIdCondicionVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getCondicionVehiculo().getIdCondicionVehiculo(),
                    Vehiculo_.CONDICION_VEHICULO, CondicionVehiculo_.ID_CONDICION_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getLicitacion() != null && vehiculo.getLicitacion().getIdLicitacion() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getLicitacion().getIdLicitacion(),
                    Vehiculo_.LICITACION, Licitacion_.ID_LICITACION));

        if(vehiculo.getAnexo() != null && vehiculo.getAnexo().getIdAnexo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getAnexo().getIdAnexo(),
                    Vehiculo_.ANEXO, Anexo_.ID_ANEXO));

        if(vehiculo.getMarca() != null && !vehiculo.getMarca().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getMarca(),
                    Vehiculo_.MARCA));

        if(vehiculo.getModelo() != null && !vehiculo.getModelo().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getModelo(),
                    Vehiculo_.MODELO));

        if(vehiculo.getTipoVehiculo() != null && !vehiculo.getTipoVehiculo().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getTipoVehiculo(),
                    Vehiculo_.TIPO_VEHICULO));

        if(vehiculo.getNoSerie() != null && !vehiculo.getNoSerie().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getNoSerie(),
                    Vehiculo_.NO_SERIE));

        if(vehiculo.getEstatusRegistro() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusRegistro(),
                    Vehiculo_.ESTATUS_REGISTRO));

        return vehiculoRepository.findAll(specification);
    }

    @Override
    public Vehiculo findById(Long idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el id: " + idVehiculo));
    }

    @Override
    public Vehiculo findByNoSerie(String noSerie) {
        return vehiculoRepository.findVehiculoActivoByNoSerie(noSerie)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el número de serie " + noSerie));
    }

    @Override
    public Vehiculo agregar(Vehiculo vehiculo) {
        return null;
    }

    @Override
    public Vehiculo editar(Vehiculo vehiculo) {
        return null;
    }

    @Override
    public Vehiculo eliminar(Vehiculo vehiculo) {
        return null;
    }

    @Override
    public Vehiculo importar(List<Vehiculo> vehiculos) {
        return null;
    }
}
