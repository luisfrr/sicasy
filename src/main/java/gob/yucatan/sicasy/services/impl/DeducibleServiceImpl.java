package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Deducible;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.iface.IDeducibleRepository;
import gob.yucatan.sicasy.services.iface.IDeducibleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeducibleServiceImpl implements IDeducibleService {

    private final IDeducibleRepository deducibleRepository;

    @Override
    public void update(Deducible deducible, String userName) {

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

            deducibleToUpdate.setModificadoPor(userName);
            deducibleToUpdate.setFechaModificacion(new Date());

            deducibleRepository.save(deducibleToUpdate);
        } else {
            deducible.setCreadoPor(userName);
            deducible.setFechaCreacion(new Date());

            deducibleRepository.save(deducible);
        }

    }
}
