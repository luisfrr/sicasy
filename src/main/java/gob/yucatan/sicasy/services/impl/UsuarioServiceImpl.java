package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IUsuarioRepository;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> findAllDynamic(Usuario usuario) {

        SearchSpecification<Usuario> specification = new SearchSpecification<>();

        if(usuario.getUsuario() != null && !usuario.getUsuario().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    usuario.getUsuario(),
                    Usuario_.USUARIO));

        if(usuario.getNombre() != null && !usuario.getNombre().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    usuario.getNombre(),
                    Usuario_.NOMBRE));

        if(usuario.getEmail() != null && !usuario.getEmail().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.MATCH,
                    usuario.getEmail(),
                    Usuario_.EMAIL));

        if(usuario.getIdRolList() != null && !usuario.getIdRolList().isEmpty())
            specification.add(new SearchCriteria(SearchOperation.IN,
                    usuario.getIdRolList(),
                    Usuario_.USUARIO_ROL_SET, UsuarioRol_.ROL, Rol_.ID_ROL));

        if(usuario.getEstatus() != null)
            specification.add(new SearchCriteria(SearchOperation.EQUAL,
                    usuario.getEstatus(),
                    Usuario_.ESTATUS));

        if(!usuario.isRolOwner())
            specification.add(new SearchCriteria(SearchOperation.NOT_EQUAL,
                    EstatusUsuario.BORRADO,
                    Usuario_.ESTATUS));

        return usuarioRepository.findAll(specification);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByUsuario(String usuario) {
        return usuarioRepository.findByUsuarioIgnoreCase(usuario);
    }

    @Override
    public void save(Usuario usuario) {
        // TODO: Implementar funcionalidad nuevo usuario
    }

    @Override
    public void delete(Usuario usuario) {
        // TODO: Implementar funcionalidad eliminar usuario
    }

    @Override
    public void update(Usuario usuario) {
        // TODO: Implementar funcionalidad editar usuario
    }


}
