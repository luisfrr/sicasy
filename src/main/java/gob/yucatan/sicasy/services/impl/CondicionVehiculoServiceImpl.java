package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.CondicionVehiculo;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.ICondicionVehiculoRepository;
import gob.yucatan.sicasy.services.iface.ICondicionVehiculoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CondicionVehiculoServiceImpl implements ICondicionVehiculoService {

    private final ICondicionVehiculoRepository condicionVehiculoRepository;

    @Override
    public List<CondicionVehiculo> findAll() {
        return condicionVehiculoRepository.findAll().stream()
                .sorted(Comparator.comparing(CondicionVehiculo::getIdCondicionVehiculo))
                .toList();
    }

    @Override
    public CondicionVehiculo findById(int id) {
        return condicionVehiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Condicion de vehiculo no encontrado"));
    }

}
