package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.EstatusVehiculo;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IEstatusVehiculoRepository;
import gob.yucatan.sicasy.services.iface.IEstatusVehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstatusVehiculoServiceImpl implements IEstatusVehiculoService {

    private final IEstatusVehiculoRepository estatusVehiculoRepository;

    @Override
    public List<EstatusVehiculo> findAll() {
        return estatusVehiculoRepository.findAll().stream()
                .sorted(Comparator.comparing(EstatusVehiculo::getIdEstatusVehiculo))
                .toList();
    }

    @Override
    public EstatusVehiculo findById(int id) {
        return estatusVehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro el id del estatus vehiculo"));
    }

}
