package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Anexo;
import gob.yucatan.sicasy.repository.iface.IAnexoRepository;
import gob.yucatan.sicasy.services.iface.IAnexoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnexoServiceImpl implements IAnexoService {

    private final IAnexoRepository anexoRepository;

    @Override
    public List<Anexo> findAllDynamic(Anexo anexo) {
        return List.of();
    }

    @Override
    public Optional<Anexo> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public Optional<Anexo> findByNombre(String nombre) {
        return Optional.empty();
    }

    @Override
    public void save(Anexo anexo) {

    }

    @Override
    public void delete(Anexo anexo) {

    }

    @Override
    public void update(Anexo anexo) {

    }
}
