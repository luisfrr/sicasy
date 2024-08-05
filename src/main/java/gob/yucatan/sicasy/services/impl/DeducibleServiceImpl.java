package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.BitacoraSiniestro;
import gob.yucatan.sicasy.business.entities.Deducible;
import gob.yucatan.sicasy.business.entities.Siniestro;
import gob.yucatan.sicasy.business.enums.EstatusRegistro;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IDeducibleRepository;
import gob.yucatan.sicasy.repository.iface.ISiniestroRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraSiniestroService;
import gob.yucatan.sicasy.services.iface.IDeducibleService;
import gob.yucatan.sicasy.utils.strings.ReplaceSymbolsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeducibleServiceImpl implements IDeducibleService {

    private final IDeducibleRepository deducibleRepository;
    private final ISiniestroRepository siniestroRepository;
    private final IBitacoraSiniestroService bitacoraSiniestroService;


    @Override
    public void update(Siniestro siniestro, Deducible deducible, String userName) {
        ReplaceSymbolsUtil.processEntity(siniestro);
        ReplaceSymbolsUtil.processEntity(deducible);
        Siniestro siniestroAnterior = siniestro.clone();
        Deducible deducibleSaved;
        if(deducible.getIdDeducible() != null) {

            Deducible deducibleToUpdate = deducibleRepository.findById(deducible.getIdDeducible())
                    .orElseThrow(() -> new NotFoundException("No se ha encontrado la información del deducible"));

            deducibleToUpdate.setTipoDeducicle(deducible.getTipoDeducicle());
            deducibleToUpdate.setValorActual(deducible.getValorActual());
            deducibleToUpdate.setCostoTotalDeducible(deducible.getCostoTotalDeducible());

            if(deducible.getFolioFacturaDeducible() != null)
                deducibleToUpdate.setFolioFacturaDeducible(deducible.getFolioFacturaDeducible());

            if(deducible.getRutaArchivoFactura() != null)
                deducibleToUpdate.setRutaArchivoFactura(deducible.getRutaArchivoFactura());

            if(deducible.getNombreArchivoFactura() != null)
                deducibleToUpdate.setNombreArchivoFactura(deducible.getNombreArchivoFactura());

            deducibleToUpdate.setEstatusRegistro(EstatusRegistro.ACTIVO);
            deducibleToUpdate.setModificadoPor(userName);
            deducibleToUpdate.setFechaModificacion(new Date());

            deducibleSaved = deducibleRepository.save(deducibleToUpdate);
        } else {
            deducible.setEstatusRegistro(EstatusRegistro.ACTIVO);
            deducible.setCreadoPor(userName);
            deducible.setFechaCreacion(new Date());

            deducibleSaved = deducibleRepository.save(deducible);
        }

        siniestro.setDeducible(deducibleSaved);

        BitacoraSiniestro bitacoraSiniestro = bitacoraSiniestroService.getBitacora("Guardar información de pago de deducible",
                siniestroAnterior, siniestro, userName);

        siniestroRepository.save(siniestro);
        bitacoraSiniestroService.save(bitacoraSiniestro);
    }
}
