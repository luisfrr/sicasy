package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.AcuseImportacion;
import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchFetch;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IAseguradoraRepository;
import gob.yucatan.sicasy.repository.iface.IPolizaRepository;
import gob.yucatan.sicasy.services.iface.IPolizaService;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolizaServiceImpl implements IPolizaService {

    private final IPolizaRepository polizaRepository;
    private final IAseguradoraRepository aseguradoraRepository;

    @Override
    public List<Poliza> findAllDynamic(Poliza poliza) {
        SearchSpecification<Poliza> specification = new SearchSpecification<>();

        if(poliza.getIdPoliza() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getIdPoliza(),
                    Poliza_.ID_POLIZA));
        }

        if(poliza.getNumeroPoliza() != null && !poliza.getNumeroPoliza().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getNumeroPoliza(),
                    Poliza_.NUMERO_POLIZA));
        }

        if(poliza.getAseguradora() != null && poliza.getAseguradora().getIdAseguradora() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getAseguradora().getIdAseguradora(),
                    Poliza_.ASEGURADORA, Aseguradora_.ID_ASEGURADORA));
        }

        if(poliza.getEstatusRegistro() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    poliza.getEstatusRegistro(),
                    Poliza_.ESTATUS_REGISTRO));
        }

        if(poliza.getIdAseguradoraList() != null && !poliza.getIdAseguradoraList().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.IN,
                    poliza.getIdAseguradoraList(),
                    Poliza_.ASEGURADORA, Aseguradora_.ID_ASEGURADORA));
        }

        if(poliza.getNumeroPolizaList() != null && !poliza.getNumeroPolizaList().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.IN,
                    poliza.getNumeroPolizaList(),
                    Poliza_.NUMERO_POLIZA));
        }

        if(poliza.isFethIncisoSet())
            specification.add(new SearchFetch(JoinType.LEFT, Poliza_.INCISO_SET));


        return polizaRepository.findAll(specification);
    }

    @Override
    public List<Poliza> findAll(Poliza poliza) {
        poliza.setEstatusRegistro(EstatusRegistro.ACTIVO);
        poliza.setFethIncisoSet(true);
        List<Poliza> polizaList = this.findAllDynamic(poliza);

        if(!polizaList.isEmpty()) {
            for (Poliza currentPoliza : polizaList) {

                double costoTotal = 0;
                double saldoTotal = 0;
                int totalIncisos = 0;

                if(currentPoliza.getIncisoSet() != null && !currentPoliza.getIncisoSet().isEmpty()) {
                    List<Inciso> incisos = new ArrayList<>(currentPoliza.getIncisoSet()).stream()
                            .filter(i -> i.getEstatusRegistro() == EstatusRegistro.ACTIVO)
                            .toList();

                    costoTotal = incisos.stream()
                            .mapToDouble(Inciso::getCosto)
                            .sum();

                    saldoTotal = incisos.stream()
                            .mapToDouble(Inciso::getSaldo)
                            .sum();

                    totalIncisos = incisos.size();
                }

                currentPoliza.setCostoTotal(costoTotal);
                currentPoliza.setSaldoTotal(saldoTotal);
                currentPoliza.setTotalIncisos(totalIncisos);
            }
        }
        return polizaList;
    }

    @Override
    public List<Poliza> findDropdown(Integer idAseguradora) {
        Date hoy = new Date();
        return polizaRepository.findByAseguradoraAndVigentes(idAseguradora, hoy, EstatusRegistro.ACTIVO);
    }

    @Override
    public Poliza findFullById(Long id) {

        Poliza poliza = Poliza.builder()
                .idPoliza(id)
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .fethIncisoSet(true)
                .build();

        return this.findAllDynamic(poliza).stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la información de la póliza"));
    }

    @Override
    public Poliza findById(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado la información de la póliza"));
    }

    @Override
    @Transactional
    public void save(Poliza poliza) {
        polizaRepository.save(poliza);
    }

    @Override
    @Transactional
    public void registrarPoliza(Poliza poliza, String username) {
        boolean alreadyExists = polizaRepository.existsByIdAseguradoraAndNumeroPolizaAndActiva(poliza.getAseguradora().getIdAseguradora(), poliza.getNumeroPoliza());

        if(alreadyExists)
            throw new BadRequestException("La póliza ya se encuentra registrada");

        if(poliza.getFechaInicioVigencia() == null) {
            throw new BadRequestException("El campo Fecha Inicio es obligatorio.");
        }

        if(poliza.getFechaFinVigencia() == null) {
            throw new BadRequestException("El campo Fecha Inicio es obligatorio.");
        }

        if(poliza.getFechaInicioVigencia().after(poliza.getFechaFinVigencia())) {
            throw new BadRequestException("No se puede registrar la póliza con fecha inicio posterior a la fecha fin.");
        }

        poliza.setEstatusRegistro(EstatusRegistro.ACTIVO);
        poliza.setFechaCreacion(new Date());
        poliza.setCreadoPor(username);

        polizaRepository.save(poliza);
    }

    @Override
    @Transactional
    public List<AcuseImportacion> importarLayoutRegistro(List<Poliza> polizas, String username) {
        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();
        this.validarLayoutRegistroPoliza(acuseImportacionList, polizas, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }

        try {
            polizaRepository.saveAll(polizas);
        } catch (Exception e) {
            acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo("Error al guardar la información")
                    .mensaje(e.getMessage())
                    .error(1)
                    .build());
        }
        return acuseImportacionList;
    }

    @Override
    @Transactional
    public void borrar(Long polizaId, String userName) {

        Poliza poliza = this.findFullById(polizaId);

        if(!poliza.getIncisoSet().isEmpty())
            throw new BadRequestException("No se puede borrar una poliza que tiene incisos.");

        poliza.setEstatusRegistro(EstatusRegistro.BORRADO);
        poliza.setFechaBorrado(new Date());
        poliza.setBorradoPor(userName);

        polizaRepository.save(poliza);
    }

    private void validarLayoutRegistroPoliza(List<AcuseImportacion> acuseImportacionList, List<Poliza> polizas, String username) {
        List<Aseguradora> aseguradoras = aseguradoraRepository.findByEstatus(EstatusRegistro.ACTIVO);

        Poliza polizaFilter = Poliza.builder()
                .idAseguradoraList(polizas.stream().map(Poliza::getIdAseguradora).distinct().toList())
                .numeroPolizaList(polizas.stream().map(Poliza::getNumeroPoliza).distinct().toList())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        List<Poliza> polizaDbList = this.findAllDynamic(polizaFilter);

        for(Poliza poliza : polizas) {

            try {
                // Validación de campos obligatorios
                if(poliza.getIdAseguradora() == null)
                    throw new BadRequestException("El campo Aseguradora es obligatorio");

                if(poliza.getNumeroPoliza() == null || poliza.getNumeroPoliza().trim().isEmpty())
                    throw new BadRequestException("El campo No. Póliza es obligatorio");

                if(poliza.getFechaInicioVigencia() == null) {
                    throw new BadRequestException("El campo Fecha Inicio es obligatorio. Revise el formato de fecha.");
                }

                if(poliza.getFechaFinVigencia() == null) {
                    throw new BadRequestException("El campo Fecha Inicio es obligatorio. Revise el formato de fecha.");
                }

                if(poliza.getTipoCobertura() == null || poliza.getTipoCobertura().isEmpty()) {
                    throw new BadRequestException("El campo Cobertura es obligatorio.");
                }

                if(polizaDbList.stream()
                        .anyMatch(p -> Objects.equals(p.getAseguradora().getIdAseguradora(), poliza.getIdAseguradora())
                                && Objects.equals(p.getNumeroPoliza(), poliza.getNumeroPoliza()))) {
                    throw new BadRequestException("La póliza ya se encuentra registrada");
                }

                Aseguradora aseguradora = aseguradoras.stream()
                        .filter(a -> Objects.equals(a.getIdAseguradora(), poliza.getIdAseguradora()))
                        .findFirst().orElseThrow(() -> new NotFoundException("No se ha logrado obtener la aseguradora: " + poliza.getIdAseguradora()));
                poliza.setAseguradora(aseguradora);


                if(poliza.getFechaInicioVigencia().after(poliza.getFechaFinVigencia())) {
                    throw new BadRequestException("No se puede registrar la póliza con fecha inicio posterior a la fecha fin.");
                }

                if(poliza.getBeneficiarioPreferente() == null)
                    poliza.setBeneficiarioPreferente("");

                poliza.setEstatusRegistro(EstatusRegistro.ACTIVO);
                poliza.setCreadoPor(username);
                poliza.setFechaCreacion(new Date());

            } catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                        .titulo(poliza.getNumeroPoliza())
                        .mensaje(e.getMessage())
                        .error(1)
                        .build());
            }
        }

        List<Poliza> polizasDuplicadas = encontrarDuplicados(polizas);
        if(!polizasDuplicadas.isEmpty()) {
            polizasDuplicadas.forEach(poliza -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(poliza.getNumeroPoliza())
                    .mensaje("La póliza esta duplicada en este layout.")
                    .error(1)
                    .build()));
        }
    }

    public static List<Poliza> encontrarDuplicados(List<Poliza> polizas) {
        Set<Poliza> unicos = new HashSet<>();
        List<Poliza> duplicados = new ArrayList<>();

        for (Poliza poliza : polizas) {
            if (!unicos.add(poliza)) { // Si no se puede agregar a 'unicos', es un duplicado
                duplicados.add(poliza);
            }
        }

        return duplicados;
    }
}
