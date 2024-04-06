package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.entities.Usuario_;
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


}
