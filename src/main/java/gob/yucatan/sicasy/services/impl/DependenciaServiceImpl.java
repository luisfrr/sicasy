package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Dependencia;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IDependenciaRepository;
import gob.yucatan.sicasy.services.iface.IDependenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DependenciaServiceImpl implements IDependenciaService {

    private final IDependenciaRepository dependenciaRepository;

    @Override
    public List<Dependencia> findAll() {
        return dependenciaRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Dependencia::getNombre))
                .toList();
    }

    @Override
    public Dependencia findById(int idDependencia) {
        return dependenciaRepository.findById(idDependencia)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la dependencia con el id: " + idDependencia));
    }
}
