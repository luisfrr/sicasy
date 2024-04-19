package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IUsuarioRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraUsuarioService;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IBitacoraUsuarioService bitacoraUsuarioService;
    private final PasswordEncoder passwordEncoder;

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
    @Transactional
    public Usuario create(Usuario usuario) {
        List<UsuarioRol> usuarioRolList = new ArrayList<>();
        Usuario finalUsuario = usuario;
        usuario.getIdRolList().forEach(idRol -> usuarioRolList.add(UsuarioRol.builder()
                        .usuario(finalUsuario)
                        .rol(Rol.builder().idRol(idRol).build())
                .build()));

        // Se agrega estatus registrado, correo no confirmado y roles
        usuario.setEstatus(EstatusUsuario.REGISTRADO);
        usuario.setCorreoConfirmado(0);
        usuario.setUsuarioRolSet(new HashSet<>(usuarioRolList));
        usuario.setContrasenia("");

        // Validaciones de creación de usuarios
        validarCreacionUsuario(usuario);

        // Generar token para validar email
        generarTokenUsuario(usuario, "Activación");

        // Guardar registro usuario
        usuario = usuarioRepository.save(usuario);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Registro de usuario", null,
                usuario, usuario.getCreadoPor());

        return usuario;
    }

    @Override
    public void delete(Usuario usuario) {
        // TODO: Implementar funcionalidad eliminar usuario
    }

    @Override
    public void update(Usuario usuario) {
        // TODO: Implementar funcionalidad editar usuario
    }


    private void validarCreacionUsuario(Usuario usuario) {
        // Validar si ya existe un usuario que no este borrado y tenga el mismo nombre de usuario
        boolean alreadyExistsByUsuario = usuarioRepository
                .existsByEstatusNotAndUsuario(EstatusUsuario.BORRADO, usuario.getUsuario());

        if(alreadyExistsByUsuario)
            throw new BadRequestException("Ya existe un usuario con ese nombre de usuario");

        // Validar si ya existe un usuario que no este borrado y tenga el mismo email
        boolean alreadyExistsByEmail = usuarioRepository
                .existsByEstatusNotAndEmail(EstatusUsuario.BORRADO, usuario.getEmail());

        if(alreadyExistsByEmail)
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
    }

    private void generarTokenUsuario(Usuario usuario, String tokenType) {
        String tokenActivacion = passwordEncoder.encode(usuario.getNombre());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Hoy
        calendar.add(Calendar.HOUR, 24);

        usuario.setToken(tokenActivacion);
        usuario.setVigenciaToken(calendar.getTime());
        usuario.setTokenType(tokenType);
    }

}
