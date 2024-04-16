package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IBItacoraUsuarioRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BitacoraUsuarioServiceImpl implements IBitacoraUsuarioService {

    private final IBItacoraUsuarioRepository bitacoraUsuarioRepository;

    @Override
    public List<BitacoraUsuario> findAllDynamic(BitacoraUsuario bitacoraUsuario) {

        SearchSpecification<BitacoraUsuario> specification = new SearchSpecification<>();

        if(bitacoraUsuario.getUsuario() != null && bitacoraUsuario.getUsuario().getIdUsuario() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    bitacoraUsuario.getUsuario().getIdUsuario(),
                    BitacoraUsuario_.USUARIO, Usuario_.ID_USUARIO));

        return bitacoraUsuarioRepository.findAll(specification);
    }

    @Override
    public List<BitacoraUsuario> findByUsuarioId(Long usuarioId) {

        BitacoraUsuario bitacoraUsuario = BitacoraUsuario.builder()
                .usuario(Usuario.builder().idUsuario(usuarioId).build())
                .build();

        return this.findAllDynamic(bitacoraUsuario);
    }

    @Override
    public void save(BitacoraUsuario usuario) {
        bitacoraUsuarioRepository.save(usuario);
    }

    @Override
    public void guardarBitacora(String accion, Usuario usuarioAnterior, Usuario usuarioNuevo, String username) {
        // TODO: Implementar bitacora usuario
    }

    @Override
    public void guardarBitacoraPermisos(Usuario usuario, List<UsuarioPermiso> usuarioPermisoAnteriorList, List<UsuarioPermiso> usuarioPermisoNuevoList, String userName) {
        // TODO: Implementar bitacora usuario - permisos
    }
}
