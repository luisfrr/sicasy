package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.BitacoraRol;
import gob.yucatan.sicasy.business.entities.BitacoraRol_;
import gob.yucatan.sicasy.business.entities.Rol;
import gob.yucatan.sicasy.business.entities.Rol_;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBitacoraRolRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraRolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitacoraRolSeviceImpl implements IBitacoraRolService {

    private final IBitacoraRolRepository bitacoraRolRepository;

    @Override
    public List<BitacoraRol> findAllDynamic(BitacoraRol bitacoraRol) {

        SearchSpecification<BitacoraRol> specification = new SearchSpecification<>();

        if(bitacoraRol.getRol() != null && bitacoraRol.getIdBitacoraRol() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraRol.getRol().getIdRol(),
                    BitacoraRol_.ROL, Rol_.ID_ROL));

        return bitacoraRolRepository.findAll(specification);
    }

    @Override
    public List<BitacoraRol> findByRolId(Long rolId) {

        BitacoraRol bitacoraRol = BitacoraRol.builder()
                .rol(Rol.builder().idRol(rolId).build())
                .build();

        return this.findAllDynamic(bitacoraRol);
    }

    @Override
    public void save(BitacoraRol bitacoraRol) {
        bitacoraRolRepository.save(bitacoraRol);
    }
}
