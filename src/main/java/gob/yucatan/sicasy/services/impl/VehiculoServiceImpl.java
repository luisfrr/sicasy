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
import gob.yucatan.sicasy.repository.iface.IEstatusVehiculoRepository;
import gob.yucatan.sicasy.repository.iface.ILicitacionRepository;
import gob.yucatan.sicasy.repository.iface.IVehiculoRepository;
import gob.yucatan.sicasy.services.iface.IVehiculoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehiculoServiceImpl implements IVehiculoService {

    private final IVehiculoRepository vehiculoRepository;
    private final ILicitacionRepository licitacionRepository;
    private final IAnexoRepository anexoRepository;
    private final IEstatusVehiculoRepository estatusVehiculoRepository;

    @Override
    public List<Vehiculo> findAllDynamic(Vehiculo vehiculo) {

        SearchSpecification<Vehiculo> specification = new SearchSpecification<>();

        if(vehiculo.getDependencia() != null && vehiculo.getDependencia().getIdDependencia() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getDependencia().getIdDependencia(),
                    Vehiculo_.DEPENDENCIA, Dependencia_.ID_DEPENDENCIA));

        if(vehiculo.getCondicionVehiculo() != null &&
                vehiculo.getCondicionVehiculo().getIdCondicionVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getCondicionVehiculo().getIdCondicionVehiculo(),
                    Vehiculo_.CONDICION_VEHICULO, CondicionVehiculo_.ID_CONDICION_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getEstatusVehiculo() != null &&
                vehiculo.getEstatusVehiculo().getIdEstatusVehiculo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusVehiculo().getIdEstatusVehiculo(),
                    Vehiculo_.ESTATUS_VEHICULO, EstatusVehiculo_.ID_ESTATUS_VEHICULO));

        if(vehiculo.getLicitacion() != null && vehiculo.getLicitacion().getIdLicitacion() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getLicitacion().getIdLicitacion(),
                    Vehiculo_.LICITACION, Licitacion_.ID_LICITACION));

        if(vehiculo.getAnexo() != null && vehiculo.getAnexo().getIdAnexo() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getAnexo().getIdAnexo(),
                    Vehiculo_.ANEXO, Anexo_.ID_ANEXO));

        if(vehiculo.getMarca() != null && !vehiculo.getMarca().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getMarca(),
                    Vehiculo_.MARCA));

        if(vehiculo.getModelo() != null && !vehiculo.getModelo().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getModelo(),
                    Vehiculo_.MODELO));

        if(vehiculo.getAnio() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getAnio(),
                    Vehiculo_.ANIO));

        if(vehiculo.getNoSerie() != null && !vehiculo.getNoSerie().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getNoSerie(),
                    Vehiculo_.NO_SERIE));

        if(vehiculo.getEstatusRegistro() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    vehiculo.getEstatusRegistro(),
                    Vehiculo_.ESTATUS_REGISTRO));

        return vehiculoRepository.findAll(specification);
    }

    @Override
    public Vehiculo findById(Long idVehiculo) {
        return vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el id: " + idVehiculo));
    }

    @Override
    public Vehiculo findFullById(Long idVehiculo) {

        Vehiculo vehiculo = findById(idVehiculo);

        if(vehiculo.getLicitacion() != null && vehiculo.getLicitacion().getIdLicitacion() != null)
            vehiculo.setNumLicitacion(vehiculo.getLicitacion().getNumeroLicitacion());
        else {
            vehiculo.setLicitacion(new Licitacion());
        }

        if(vehiculo.getAnexo() != null && vehiculo.getAnexo().getIdAnexo() != null)
            vehiculo.setAnexoValue(vehiculo.getAnexo().getNombre());
        else {
            vehiculo.setAnexo(new Anexo());
        }

        if(vehiculo.getDependenciaAsignada() == null)
            vehiculo.setDependenciaAsignada(new Dependencia());

        return vehiculo;
    }

    @Override
    public List<Vehiculo> findAllByNoSerie(List<String> noSerieList) {
        return vehiculoRepository.findVehiculosActivosByNoSerie(noSerieList);
    }

    @Override
    public List<String> findDistinctMarcas() {
        return vehiculoRepository.findDistinctMarcasByEstatusActivo();
    }

    @Override
    public List<String> findDistinctModelo(String marca) {
        return vehiculoRepository.findDistinctModeloByEstatusActivo(marca);
    }

    @Override
    public List<Integer> findDistinctAnio(String marca, String modelo) {
        return vehiculoRepository.findDistinctAnioByEstatusActivo(marca, modelo);
    }

    @Override
    public Vehiculo findByNoSerie(String noSerie) {
        return vehiculoRepository.findVehiculoActivoByNoSerie(noSerie)
                .orElseThrow(() -> new NotFoundException("No se ha encontrado el vehículo con el número de serie " + noSerie));
    }

    @Override
    public Vehiculo agregar(Vehiculo vehiculo) {
        Integer ESTATUS_VEHICULO_REGISTRADO = 1;

        // Validar si ya existe un vehiculo con ese no. de serie
        if(vehiculoRepository.existsByEstatusRegistroActivoAndNoSerie(vehiculo.getNoSerie()))
            throw new BadRequestException("Ya existe un vehículo con ese número de serie.");

        if(vehiculo.getAnexo().getIdAnexo() == null)
            vehiculo.setAnexo(null);
        if(vehiculo.getLicitacion().getIdLicitacion() == null)
            vehiculo.setLicitacion(null);
        if(vehiculo.getDependenciaAsignada().getIdDependencia() == null)
            vehiculo.setDependenciaAsignada(null);

        // Se agrega el estatus vehículo
        vehiculo.setEstatusVehiculo(EstatusVehiculo.builder().idEstatusVehiculo(ESTATUS_VEHICULO_REGISTRADO).build());
        vehiculo.setEstatusRegistro(EstatusRegistro.ACTIVO);

        return vehiculoRepository.save(vehiculo);
    }

    @Override
    public void editar(Vehiculo vehiculo) {

        Vehiculo vehiculoToUpdate = this.findById(vehiculo.getIdVehiculo());

        // Validar si ya existe un vehiculo con ese no. de serie
        if(vehiculoRepository.existsByEstatusRegistroActivoAndNoSerieAndIdVehiculoNot(vehiculo.getNoSerie(),
                vehiculo.getIdVehiculo()))
            throw new BadRequestException("Ya existe un vehículo con ese número de serie.");

        if(vehiculo.getLicitacion().getIdLicitacion() == null)
            vehiculoToUpdate.setLicitacion(null);
        else
            vehiculoToUpdate.setLicitacion(vehiculo.getLicitacion());

        if(vehiculo.getAnexo().getIdAnexo() == null)
            vehiculoToUpdate.setAnexo(null);
        else
            vehiculoToUpdate.setAnexo(vehiculo.getAnexo());

        if(vehiculo.getDependenciaAsignada().getIdDependencia() == null)
            vehiculoToUpdate.setDependenciaAsignada(null);
        else
            vehiculoToUpdate.setDependenciaAsignada(vehiculo.getDependenciaAsignada());

        vehiculoToUpdate.setDependencia(vehiculo.getDependencia());
        vehiculoToUpdate.setNoSerie(vehiculo.getNoSerie());
        vehiculoToUpdate.setPlaca(vehiculo.getPlaca());
        vehiculoToUpdate.setMarca(vehiculo.getMarca());
        vehiculoToUpdate.setModelo(vehiculo.getModelo());
        vehiculoToUpdate.setAnio(vehiculo.getAnio());
        vehiculoToUpdate.setNoMotor(vehiculo.getNoMotor());
        vehiculoToUpdate.setColor(vehiculo.getColor());
        vehiculoToUpdate.setCondicionVehiculo(vehiculo.getCondicionVehiculo());
        vehiculoToUpdate.setNoFactura(vehiculo.getNoFactura());
        vehiculoToUpdate.setMontoFactura(vehiculo.getMontoFactura());
        vehiculoToUpdate.setRentaMensual(vehiculo.getRentaMensual());
        vehiculoToUpdate.setDescripcionVehiculo(vehiculo.getDescripcionVehiculo());
        vehiculoToUpdate.setResguardante(vehiculo.getResguardante());
        vehiculoToUpdate.setAreaResguardante(vehiculo.getAreaResguardante());
        vehiculoToUpdate.setProveedor(vehiculo.getProveedor());

        vehiculoToUpdate.setModificadoPor(vehiculo.getModificadoPor());
        vehiculoToUpdate.setFechaModificacion(new Date());

        vehiculoRepository.save(vehiculoToUpdate);
    }

    @Override
    @Transactional
    public List<AcuseImportacion> importar(List<Vehiculo> vehiculos, Integer idDependencia, String username) {

        List<AcuseImportacion> acuseImportacionList = new ArrayList<>();

        this.validarImportacion(acuseImportacionList, vehiculos, idDependencia, username);

        if(acuseImportacionList.stream().anyMatch(acuseImportacion -> acuseImportacion.getError() == 1)) {
            return acuseImportacionList;
        }

        try {
            vehiculoRepository.saveAll(vehiculos);
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
    public void solicitarAutorizacion(List<Long> idVehiculoList, String username) {
        Integer ESTATUS_VEHICULO_POR_AUTORIZAR = 2;
        this.cambioEstatus(ESTATUS_VEHICULO_POR_AUTORIZAR, idVehiculoList, null, username);
    }

    @Override
    public void autorizarSolicitud(List<Long> idVehiculoList, String username) {
        Integer ESTATUS_VEHICULO_ACTIVO = 1;
        this.cambioEstatus(ESTATUS_VEHICULO_ACTIVO, idVehiculoList, null, username);
    }

    @Override
    public void rechazarSolicitud(List<Long> idVehiculoList, String motivo, String username) {
        Integer ESTATUS_VEHICULO_RECHAZADO= 3;
        this.cambioEstatus(ESTATUS_VEHICULO_RECHAZADO, idVehiculoList, motivo, username);
    }

    @Override
    public void cancelarSolicitud(List<Long> idVehiculoList, String motivo, String username) {
        Integer ESTATUS_VEHICULO_CANCELADO = 4;
        this.cambioEstatus(ESTATUS_VEHICULO_CANCELADO, idVehiculoList, motivo, username);
    }

    private void validarImportacion(List<AcuseImportacion> acuseImportacionList, List<Vehiculo> vehiculos, Integer idDependencia, String username) {
        Integer ESTATUS_VEHICULO_REGISTRADO = 1;

        for (Vehiculo vehiculo : vehiculos) {
            try {
                // Validacion de campos obligatorios
                if(vehiculo.getNoSerie() == null || vehiculo.getNoSerie().isEmpty())
                    throw new BadRequestException("El campo No. Serie es obligatorio");

                if(vehiculo.getPlaca() == null || vehiculo.getPlaca().isEmpty())
                    throw new BadRequestException("El campo Placa es obligatorio");

                if(vehiculo.getMarca() == null || vehiculo.getMarca().isEmpty())
                    throw new BadRequestException("El campo Marca es obligatorio");

                if(vehiculo.getModelo() == null || vehiculo.getModelo().isEmpty())
                    throw new BadRequestException("El campo Modelo es obligatorio");

                if(vehiculo.getAnio() == null)
                    throw new BadRequestException("El campo Año es obligatorio");

                if(vehiculo.getNoMotor() == null || vehiculo.getNoMotor().isEmpty())
                    throw new BadRequestException("El campo No. Motor es obligatorio");

                if(vehiculo.getColor() == null || vehiculo.getColor().isEmpty())
                    throw new BadRequestException("El campo Color es obligatorio");

                if(vehiculo.getCondicionId() == null)
                    throw new BadRequestException("El campo Condición es obligatorio");
                else if(vehiculo.getCondicionId() < 1 || vehiculo.getCondicionId() > 3)
                    throw new BadRequestException("El campo Condición solo acepta valor 1, 2 o 3.");


                if(vehiculo.getNoFactura() == null || vehiculo.getNoFactura().isEmpty())
                    throw new BadRequestException("El campo No. Factura es obligatorio");

                if(vehiculo.getMontoFactura() == null)
                    throw new BadRequestException("El campo Valor Factura es obligatorio");

                if(vehiculo.getRentaMensual() == null)
                    throw new BadRequestException("El campo Renta Mensual es obligatorio");

                // Se agregan los campos que hacen referencia a otras tablas
                vehiculo.setDependencia(Dependencia.builder().idDependencia(idDependencia).build());

                if(vehiculo.getCondicionId() != null)
                    vehiculo.setCondicionVehiculo(CondicionVehiculo.builder()
                            .idCondicionVehiculo(vehiculo.getCondicionId())
                            .build());

                if(vehiculo.getNumLicitacion() != null) {
                    Licitacion licitacion = licitacionRepository
                            .findByNumeroLicitacionAndEstatusRegistro(vehiculo.getNumLicitacion(),
                                    EstatusRegistro.ACTIVO)
                            .orElseThrow(() -> new NotFoundException("No se encontro la licitacion con el número de licitacion: "
                                    + vehiculo.getNumLicitacion()));
                    vehiculo.setLicitacion(licitacion);
                }

                if(vehiculo.getNumLicitacion() != null) {
                    Anexo anexo = anexoRepository
                            .findAnexoActivoByNombreAndLicitacion(vehiculo.getAnexoValue(),
                                    vehiculo.getLicitacion())
                            .orElseThrow(() -> new NotFoundException("No se encontro el anexo. Anexo: " + vehiculo.getAnexoValue() +
                                    ", número de licitación: "+ vehiculo.getNumLicitacion()));
                    vehiculo.setAnexo(anexo);
                }

                vehiculo.setEstatusVehiculo(EstatusVehiculo.builder()
                        .idEstatusVehiculo(ESTATUS_VEHICULO_REGISTRADO)
                        .build());
                vehiculo.setEstatusRegistro(EstatusRegistro.ACTIVO);
                vehiculo.setFechaCreacion(new Date());
                vehiculo.setCreadoPor(username);

            } catch (Exception e) {
                acuseImportacionList.add(AcuseImportacion.builder()
                                .titulo(vehiculo.getNoSerie())
                                .mensaje(e.getMessage())
                                .error(1)
                        .build());
            }
        }

        List<String> noSerieList = vehiculos.stream()
                .map(Vehiculo::getNoSerie)
                .distinct()
                .toList();
        List<Vehiculo> existsVehiculoByNoSerie = vehiculoRepository.findVehiculosActivosByNoSerie(noSerieList);

        // Si la lista no es vacia, quiere decir ya existen algunos vehiculos registrados
        if(!existsVehiculoByNoSerie.isEmpty()) {
            existsVehiculoByNoSerie.forEach(v -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(v.getNoSerie())
                    .mensaje("Ya existe un vehículo registrado con ese número de serie")
                    .error(1)
                    .build()));
        }

        // Si es diferente quiere decir que un número de serie se está duplicando en el layout
        if(noSerieList.size() != vehiculos.size()) {
            List<String> noSerieDuplicadosLayout = vehiculos.stream()
                    .map(Vehiculo::getNoSerie)
                    .filter(noSerie -> !noSerieList.contains(noSerie))
                    .toList();
            noSerieDuplicadosLayout.forEach(noSerie -> acuseImportacionList.add(AcuseImportacion.builder()
                    .titulo(noSerie)
                    .mensaje("El número de serie esta duplicado en este layout.")
                    .error(1)
                    .build()));
        }
    }

    private void cambioEstatus(Integer estatus, List<Long> idVehiculoList, String motivo, String username) {
        List<Vehiculo> vehiculoToUpdateList = vehiculoRepository.findAllByIdVehiculo(idVehiculoList);

        if(!vehiculoToUpdateList.isEmpty()) {

            EstatusVehiculo estatusPorAutorizar =estatusVehiculoRepository.findById(estatus)
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado el estatus solicitado."));

            // Se les agrega el estatus Por Autorizar
            for (Vehiculo vehiculo : vehiculoToUpdateList) {
                vehiculo.setEstatusVehiculo(estatusPorAutorizar);
                vehiculo.setObservaciones(motivo);
                vehiculo.setModificadoPor(username);
                vehiculo.setFechaModificacion(new Date());
            }

            vehiculoRepository.saveAll(vehiculoToUpdateList);
        } else {
            throw new BadRequestException("No se han recibido los vehículos por actualizar.");
        }
    }
}
