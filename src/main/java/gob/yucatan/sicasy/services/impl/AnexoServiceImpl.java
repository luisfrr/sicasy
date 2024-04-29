package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAnexoRepository;
import gob.yucatan.sicasy.services.iface.IAnexoService;
import gob.yucatan.sicasy.services.iface.ILicitacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnexoServiceImpl implements IAnexoService {

    private final IAnexoRepository anexoRepository;

    @Override
    public List<Anexo> findAllDynamic(Anexo anexo) {

        SearchSpecification<Anexo> searchSpecification = new SearchSpecification<>();

//        searchSpecification.add(new SearchFetch(JoinType.INNER, Anexo_.LICITACION));

        if (anexo.getNombre() != null)
            searchSpecification.add(new SearchCriteria(SearchOperation.MATCH,
                    anexo.getNombre(),
                    Anexo_.NOMBRE));

        if (anexo.getLicitacion() != null && anexo.getLicitacion().getNumeroLicitacion() != null)
            searchSpecification.add(new SearchCriteria(SearchOperation.MATCH,
                    anexo.getLicitacion().getNumeroLicitacion(),
                    Anexo_.LICITACION, Licitacion_.NUMERO_LICITACION));

        if (anexo.getEstatusRegistro() != null )
            searchSpecification.add(new SearchCriteria(SearchOperation.EQUAL,
                    anexo.getEstatusRegistro(),
                    Anexo_.ESTATUS_REGISTRO));

        return anexoRepository.findAll(searchSpecification);
    }

    @Override
    public Optional<Anexo> findById(Long id) {
        return anexoRepository.findById(id);
    }

    @Override
    public List<Anexo> findByLicitacion(Licitacion licitacion) {
        return anexoRepository.findByLicitacionAndEstatusRegistro(licitacion, EstatusRegistro.ACTIVO);
    }

    @Override
    public List<Anexo> findByNombreAndLicitacion(String nombre, Licitacion licitacion) {
        return anexoRepository.findByNombreAndEstatusRegistroAndLicitacion(nombre, EstatusRegistro.ACTIVO, licitacion);
    }


    @Override
    public Optional<Anexo> findByNombre(String nombre) {
        return anexoRepository.findByNombre(nombre);
    }

    @Override
    public void save(Anexo anexo) {

        anexo.setEstatusRegistro(EstatusRegistro.ACTIVO);
        anexo.setFechaCreacion(new Date());
        anexoRepository.save(anexo);

    }

    @Override
    public void delete(Anexo anexo) {

        Optional<Anexo> optionalAnexo = anexoRepository.findById(anexo.getIdAnexo());

        if (optionalAnexo.isEmpty())
            throw new NotFoundException("No se encontró ninguna información");


        Anexo anexoToUpdate = optionalAnexo.get();
        anexoToUpdate.setFechaBorrado(new Date());
        anexoToUpdate.setBorradoPor(anexo.getBorradoPor());
        anexoToUpdate.setEstatusRegistro(EstatusRegistro.BORRADO);
        anexoRepository.save(anexoToUpdate);

    }

    @Override
    public void update(Anexo anexo) {

        Optional<Anexo> optionalAnexo = anexoRepository.findById(anexo.getIdAnexo());

        if (optionalAnexo.isPresent()) {
            Anexo anexoToUpdate = optionalAnexo.get();
            anexoToUpdate.setModificadoPor(anexo.getModificadoPor());
            anexoToUpdate.setFechaModificacion(new Date());
            anexoToUpdate.setNombre(anexo.getNombre());
            anexoToUpdate.setDescripcion(anexo.getDescripcion());
            anexoToUpdate.setEstatusRegistro(anexo.getEstatusRegistro());
            anexoToUpdate.setFechaInicio(anexo.getFechaInicio());
            anexoToUpdate.setFechaFinal(anexo.getFechaFinal());
            anexoToUpdate.setFechaFirma(anexo.getFechaFirma());
            anexoToUpdate.setLicitacion(anexo.getLicitacion());
            anexoToUpdate.setRutaArchivo(anexo.getRutaArchivo());
            anexoRepository.save(anexoToUpdate);
        } else {
            throw new NotFoundException("Información no encontrada");
        }


    }
}
