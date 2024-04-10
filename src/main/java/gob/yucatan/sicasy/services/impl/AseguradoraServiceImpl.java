package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Aseguradora;
import gob.yucatan.sicasy.business.entities.Aseguradora_;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAseguradoraRepository;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AseguradoraServiceImpl implements IAseguradoraService {

    private final IAseguradoraRepository aseguradoraRepository;

    @Override
    public List<Aseguradora> findAllDynamic(Aseguradora aseguradora) {

        SearchSpecification<Aseguradora> searchSpecification =new SearchSpecification<>();

        if (aseguradora.getNombre() != null && !aseguradora.getNombre().isEmpty()) {
            searchSpecification.add(new SearchCriteria(SearchOperation.MATCH,
                    aseguradora.getNombre() ,
                    Aseguradora_.NOMBRE));
        }

        if (aseguradora.getRepresentante() != null && !aseguradora.getRepresentante().isEmpty()) {
            searchSpecification.add(new SearchCriteria(SearchOperation.MATCH,
                    aseguradora.getRepresentante(),
                    Aseguradora_.REPRESENTANTE));
        }

        return aseguradoraRepository.findAll(searchSpecification);
    }

    @Override
    public Optional<Aseguradora> findById(Integer id) {
        return aseguradoraRepository.findById(id);
    }

    @Override
    public Optional<Aseguradora> findByNombre(String nombre) {
        return aseguradoraRepository.findByNombreIgnoreCase(nombre);
    }
}
