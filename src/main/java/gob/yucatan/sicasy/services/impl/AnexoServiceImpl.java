package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAnexoRepository;
import gob.yucatan.sicasy.repository.iface.ILicitacionRepository;
import gob.yucatan.sicasy.services.iface.IAnexoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnexoServiceImpl implements IAnexoService {

    private final IAnexoRepository anexoRepository;
    private final ILicitacionRepository licitacionRepository;

    @SafeVarargs
    private static <T> Predicate<T>
    distinctByKeys(final Function<? super T, ?>... keyExtractors)
    {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();

        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());

            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }

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

    @Override
    public List<AcuseImportacion> importar(List<Anexo> anexos, String username) {
        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();

        this.validarImportacion(acuseImportacionList, anexos, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }

        try {
            anexoRepository.saveAll(anexos);
        } catch (Exception e) {
            acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo("Error al guardar la información")
                    .mensaje(e.getMessage())
                    .error(1)
                    .build());
        }

        return acuseImportacionList;
    }

    private void validarImportacion( List<AcuseImportacion> acuseImportacionList ,List<Anexo> anexos, String username) {

        for (Anexo anexo : anexos) {
            try {
                // validar campos obligatorios
                if(anexo.getNumLicitacionString() == null || anexo.getNumLicitacionString().isEmpty())
                    throw new BadRequestException("El campo Num. Licitación es obligatorio");

                if(anexo.getNombre() == null || anexo.getNombre().isEmpty())
                    throw new BadRequestException("El campo Nombre es obligatorio");

                if (anexo.getFechaInicio() != null && anexo.getFechaFinal() != null){
                    if (anexo.getFechaInicio().after(anexo.getFechaFinal())){
                        throw new BadRequestException("La fecha de inicio no puede ser una fecha posterior a la fecha final");
                    }
                }

                Licitacion licitacion = licitacionRepository
                        .findByNumeroLicitacionAndEstatusRegistro(anexo.getNumLicitacionString(),
                                EstatusRegistro.ACTIVO)
                        .orElseThrow(() -> new NotFoundException("No se encontro la licitacion con el número de licitacion: "
                                + anexo.getNumLicitacionString()));
                anexo.setLicitacion(licitacion);

                anexo.setEstatusRegistro(EstatusRegistro.ACTIVO);
                anexo.setFechaCreacion(new Date());
                anexo.setCreadoPor(username);


            }catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                        .titulo(anexo.getNombre())
                        .mensaje(e.getMessage())
                        .error(1)
                        .build());
            }
        }

        // validar que no existan los anexos de la lista
        List<String> nombresAnexosLista = anexos.stream()
                .map(Anexo :: getNombre )
                .distinct()
                .toList();

        List<Anexo> anexosActivosLista = anexoRepository.findAnexosActivosByNombre(nombresAnexosLista);

        if (!anexosActivosLista.isEmpty()){
            anexosActivosLista.forEach(v -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(v.getNombre())
                    .mensaje("Ya existe un anexo registrado con ese nombre")
                    .error(1)
                    .build()));
        }

        // validar que si esten registrados las licitaciones
        List<String> numLicitaciones = anexos.stream()
                .map(Anexo::getNumLicitacionString)
                .distinct()
                .toList();

        List<Licitacion> licitacionList = licitacionRepository.findLicitacionesActivasByNumeroLicitacion(numLicitaciones);

        // si esta vacio es que ninguna licitacion existe en la base
        if (licitacionList.isEmpty()){
            acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo("Error")
                    .mensaje("Estas licitaciones no se encuentran registradas.")
                    .error(1)
                    .build());
        }

        // alguna licitacion no esta registrada
        if (!licitacionList.isEmpty() && licitacionList.size() != numLicitaciones.size()){
            acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo("Error")
                    .mensaje("Una o mas licitaciones no se encuentran registradas.")
                    .error(1)
                    .build());
        }

        // que no se repitan los pares licitacion-anexo
        List<Anexo> anexoFilterList = anexos.stream()
                .filter(distinctByKeys(Anexo::getNombre, Anexo ::getNumLicitacionString))
                .toList();

        if (!anexoFilterList.isEmpty()){
            for (Anexo anexo : anexoFilterList){
                List<Anexo> anexos1 = anexos.stream()
                        .filter(a -> a.getNumLicitacionString().equals(anexo.getNumLicitacionString())
                        && a.getNombre().equals(anexo.getNombre()))
                        .toList();

                // aqui se valida si existen mas de un par distinto de clave numlicitacion y nombre_anexo
                if (!anexos1.isEmpty() && anexos1.size() > 1){
                    acuseImportacionList.add(AcuseImportacion.builder()
                            .titulo(anexo.getNombre())
                            .mensaje("Este registro se encuentra duplicado en el archivo")
                            .error(1)
                            .build());
                }
            }
        }

    }




}

