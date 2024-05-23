package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IIncisoRepository;
import gob.yucatan.sicasy.repository.iface.IPolizaRepository;
import gob.yucatan.sicasy.services.iface.IIncisoService;
import gob.yucatan.sicasy.utils.date.DateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncisoServiceImpl implements IIncisoService {

    private final Integer ESTATUS_REGISTRADO = 1;

    private final IIncisoRepository incisoRepository;
    private final IPolizaRepository polizaRepository;

    @Override
    public List<Inciso> findAllDynamic(Inciso inciso) {
        SearchSpecification<Inciso> specification = new SearchSpecification<>();

        if(inciso.getPoliza() != null && inciso.getPoliza().getIdPoliza() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getPoliza().getIdPoliza(),
                    Inciso_.POLIZA, Poliza_.ID_POLIZA));
        }

        if(inciso.getEstatusRegistro() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getEstatusRegistro(),
                    Inciso_.ESTATUS_REGISTRO));
        }

        if(inciso.getEstatusInciso() != null && inciso.getEstatusInciso().getIdEstatusPoliza() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getEstatusInciso().getIdEstatusPoliza(),
                    Inciso_.ESTATUS_INCISO, EstatusInciso_.ID_ESTATUS_POLIZA));
        }

        if(inciso.getVehiculo() != null && inciso.getVehiculo().getIdVehiculo() != null) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getVehiculo().getIdVehiculo(),
                    Inciso_.VEHICULO, Vehiculo_.ID_VEHICULO));
        }

        if(inciso.getTipoCobertura() != null && !inciso.getTipoCobertura().isEmpty()) {
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    inciso.getTipoCobertura(),
                    Inciso_.TIPO_COBERTURA));
        }

        return incisoRepository.findAll(specification);
    }

    @Override
    public List<Inciso> findByIdPoliza(Long idPoliza) {
        Inciso inciso = Inciso.builder()
                .poliza(Poliza.builder().idPoliza(idPoliza).build())
                .estatusRegistro(EstatusRegistro.ACTIVO)
                .build();
        return this.findAllDynamic(inciso);
    }

    @Override
    public Inciso findById(Long id) {
        return incisoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se ha logrado obtener la información del inciso."));
    }

    @Override
    @Transactional
    public void generarEndosoAlta(Inciso inciso, String username) {

        Long idPoliza = inciso.getPoliza().getIdPoliza();
        Poliza poliza = polizaRepository.findById(idPoliza)
                .orElseThrow(() -> new NotFoundException("No se ha logrado obtener la póliza."));
        inciso.setPoliza(poliza);

        // Validación de campos
        if(inciso.getPoliza() == null || inciso.getPoliza().getIdPoliza() == null)
            throw new BadRequestException("El campo Póliza es obligatorio");

        if(inciso.getInciso() == null || inciso.getInciso().isEmpty())
            throw new BadRequestException("El campo Inciso es obligatorio");

        if(inciso.getVehiculo() == null
                || inciso.getVehiculo().getNoSerie() == null
                || inciso.getVehiculo().getNoSerie().isEmpty())
            throw new BadRequestException("El campo No. Serie es obligatorio");

        if(inciso.getFolioFactura() == null || inciso.getFolioFactura().isEmpty())
            throw new BadRequestException("El campo Folio factura es obligatorio");

        if(inciso.getFechaInicioVigencia() == null)
            throw new BadRequestException("El campo Fecha inicio de vigencia es obligatorio");

        if(inciso.getFechaFinVigencia() == null)
            throw new BadRequestException("El campo Fecha fin de vigencia es obligatorio");

        if(inciso.getCosto() < 0)
            throw new BadRequestException("El campo Costo no puede ser menor a 0.");

        if(DateValidator.isDateBetween(poliza.getFechaInicioVigencia(), poliza.getFechaFinVigencia(), inciso.getFechaInicioVigencia()))
            throw new BadRequestException("La fecha inicio de vigencia debe estar entre la fecha inicio y fecha fin de la póliza");

        if(DateValidator.isDateBetween(poliza.getFechaInicioVigencia(), poliza.getFechaFinVigencia(), inciso.getFechaFinVigencia()))
            throw new BadRequestException("La fecha fin de vigencia debe estar entre la fecha inicio y fecha fin de la póliza");

        if(inciso.getFechaInicioVigencia().after(inciso.getFechaFinVigencia()))
            throw new BadRequestException("La fecha inicio de vigencia no puede ser posterior a la fecha fin de vigencia");

        // TODO: Validar si ya existe el inciso

        // Se agrega el saldo
        if(inciso.getCosto() > 0)
            inciso.setSaldo(-inciso.getCosto());
        else
            inciso.setSaldo(0d);

        inciso.setEstatusInciso(EstatusInciso.builder().idEstatusPoliza(ESTATUS_REGISTRADO).build());

        inciso.setEstatusRegistro(EstatusRegistro.ACTIVO);
        inciso.setFechaCreacion(new Date());
        inciso.setCreadoPor(username);

        incisoRepository.save(inciso);
    }

    @Override
    public void importarEndosoAlta(List<Inciso> incisos, String username) {

    }
}
