package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.ILicitacionRepository;
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
public class LicitacionServiceImpl implements ILicitacionService {

    private final ILicitacionRepository licitacionRepository;

    @Override
    public List<Licitacion> findAllDynamic(Licitacion licitacion) {

        SearchSpecification<Licitacion> specification = new SearchSpecification<>();

        if (licitacion.getNombre() != null && !licitacion.getNombre().trim().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    licitacion.getNombre(),
                    Licitacion_.NOMBRE));

        if (licitacion.getEstatusRegistro() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    licitacion.getEstatusRegistro(),
                    Licitacion_.ESTATUS_REGISTRO));

        if (licitacion.getNumeroLicitacion() != null && !licitacion.getNumeroLicitacion().trim().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    licitacion.getNumeroLicitacion(),
                    Licitacion_.NUMERO_LICITACION));




        return licitacionRepository.findAll(specification);
    }

    @Override
    public Optional<Licitacion> findById(Integer id) {
        return licitacionRepository.findById(id);
    }

    @Override
    public Optional<Licitacion> findByNombre(String nombre) {
        return licitacionRepository.findByNombreIgnoreCase(nombre);
    }

    @Override
    public Optional<Licitacion> findByNumeroLicitacion(String numeroLicitacion) {
        return licitacionRepository.findByNumeroLicitacionAndEstatusRegistro(numeroLicitacion, EstatusRegistro.ACTIVO);
    }

    @Override
    public List<Licitacion> findAllLicitacionActive() {
        return licitacionRepository.findByEstatusRegistro(EstatusRegistro.ACTIVO);
    }

    @Override
    public void save(Licitacion licitacion) {
        licitacion.setEstatusRegistro(EstatusRegistro.ACTIVO);
        licitacion.setFechaCreacion(new Date());

        licitacionRepository.save(licitacion);
    }

    @Override
    public void delete(Licitacion licitacion) {

        licitacion.setFechaBorrado(new Date());
        licitacion.setEstatusRegistro(EstatusRegistro.BORRADO);
        licitacionRepository.save(licitacion);

    }

    @Override
    public void update(Licitacion licitacion) {

        Optional<Licitacion> licitacionOptional = licitacionRepository.findById(licitacion.getIdLicitacion());

        if (licitacionOptional.isEmpty())
            throw new NotFoundException("No se ha encontrado información de esta Licitación.");

        Licitacion licitacionToUpdate = licitacionOptional.get();
        licitacionToUpdate.setNombre(licitacion.getNombre());
        licitacionToUpdate.setDescripcion(licitacion.getDescripcion());
        licitacionToUpdate.setFechaInicio(licitacion.getFechaInicio());
        licitacionToUpdate.setFechaFinal(licitacion.getFechaFinal());
        licitacionToUpdate.setFechaModificacion(new Date());
        licitacionToUpdate.setModificadoPor(licitacion.getModificadoPor());
        licitacionToUpdate.setRutaArchivo(licitacion.getRutaArchivo());

        licitacionRepository.save(licitacionToUpdate);


    }
}
