package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Aseguradora;
import gob.yucatan.sicasy.business.entities.Aseguradora_;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAseguradoraRepository;
import gob.yucatan.sicasy.services.iface.IAseguradoraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
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

        if (aseguradora.getEstatus() != null )
            searchSpecification.add(new SearchCriteria(SearchOperation.EQUAL,
                    aseguradora.getEstatus(),
                    Aseguradora_.ESTATUS));

        return aseguradoraRepository.findAll(searchSpecification);
    }

    @Override
    public List<Aseguradora> findAll() {
        Aseguradora aseguradora = Aseguradora.builder()
                .estatus(EstatusRegistro.ACTIVO)
                .build();
        return this.findAllDynamic(aseguradora);
    }

    @Override
    public Optional<Aseguradora> findById(Integer id) {
        return aseguradoraRepository.findById(id);
    }

    @Override
    public Optional<Aseguradora> findByNombre(String nombre) {
        return aseguradoraRepository.findByNombreIgnoreCase(nombre);
    }

    @Override
    public void save(Aseguradora aseguradora) {
        aseguradora.setEstatus(EstatusRegistro.ACTIVO);
        aseguradoraRepository.save(aseguradora);

    }

    @Override
    public void delete(Aseguradora aseguradora) {

        aseguradora.setEstatus(EstatusRegistro.BORRADO);
        aseguradora.setFechaBorrado(new Date());
        aseguradoraRepository.save(aseguradora);

    }

    @Override
    public void update(Aseguradora aseguradora) {
        aseguradoraRepository.save(aseguradora);

    }
}
