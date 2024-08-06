package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.*;
import gob.yucatan.sicasy.business.enums.EstatusUsuario;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.business.exceptions.NotFoundException;
import gob.yucatan.sicasy.repository.criteria.SearchCriteria;
import gob.yucatan.sicasy.repository.criteria.SearchOperation;
import gob.yucatan.sicasy.repository.criteria.SearchSpecification;
import gob.yucatan.sicasy.repository.iface.IRolRepository;
import gob.yucatan.sicasy.repository.iface.IUsuarioRepository;
import gob.yucatan.sicasy.repository.iface.IUsuarioRolRepository;
import gob.yucatan.sicasy.services.iface.IBitacoraUsuarioService;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import gob.yucatan.sicasy.utils.strings.ReplaceSymbolsUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IBitacoraUsuarioService bitacoraUsuarioService;
    private final IUsuarioRolRepository usuarioRolRepository;
    private final IRolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Usuario> findAllDynamic(Usuario usuario) {
        ReplaceSymbolsUtil.processEntity(usuario);
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
    public Optional<Usuario> findByToken(String token) {
        return usuarioRepository.findByToken(token);
    }

    @Override
    @Transactional
    public Usuario create(Usuario usuario) {
        this.validarCampos(usuario);
        List<UsuarioRol> usuarioRolList = new ArrayList<>();
        Usuario finalUsuario = usuario;
        usuario.getIdRolList().forEach(idRol -> {
            Rol rol = rolRepository.findById(idRol).orElse(null);
            usuarioRolList.add(UsuarioRol.builder()
                    .usuario(finalUsuario)
                    .rol(rol)
                    .build());
        });

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
    @Transactional
    public void delete(Usuario usuario) {
        String borradoPor = usuario.getBorradoPor();

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuario.getIdUsuario());

        if(usuarioOptional.isEmpty())
            throw new NotFoundException("Usuario no encontrado");

        usuario = usuarioOptional.get();

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        // Se asigna estatus borrado
        usuario.setEstatus(EstatusUsuario.BORRADO);
        usuario.setFechaBorrado(new Date());
        usuario.setBorradoPor(borradoPor);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Eliminar usuario", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void update(Usuario usuario) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuario.getIdUsuario());

        if(usuarioOptional.isEmpty())
            throw new NotFoundException("Usuario no encontrado");

        this.validarCampos(usuario);

        // Roles que se van a reemplazar por los nuevos
        final Usuario usuarioAnterior = usuarioRepository.findById(usuario.getIdUsuario()).orElse(null);
        assert usuarioAnterior != null;
        Set<UsuarioRol> usuarioRolToDelete = usuarioAnterior.getUsuarioRolSet();

        // Roles nuevos
        List<UsuarioRol> usuarioRolNuevoList = new ArrayList<>();
        usuario.getIdRolList().forEach(idRol -> {
            Rol rol = rolRepository.findById(idRol).orElse(null);
            usuarioRolNuevoList.add(UsuarioRol.builder()
                    .usuario(usuario)
                    .rol(rol)
                    .build());
        });

        // Se asigna de fecha de modificacion y los nuevos roles
        usuario.setFechaModificacion(new Date());
        usuario.setUsuarioRolSet(new HashSet<>(usuarioRolNuevoList));

        // Validaciones de actualización de usuario
        validarActualizacionUsuario(usuario);

        // Se guarda la bitácora
        bitacoraUsuarioService.guardarBitacora("Editar usuario", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        // Se borran los roles
        usuarioRolRepository.deleteAll(usuarioRolToDelete);

        // Se guardan los datos del usuario y sus roles
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void activarCuenta(Usuario usuario) {
        String username = usuario.getUsuario();
        String password = usuario.getContrasenia();
        String passwordEncrypted = passwordEncoder.encode(password);

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora
        
        // Se modifica el usuario
        usuario.setEstatus(EstatusUsuario.ACTIVO);
        usuario.setContrasenia(passwordEncrypted);
        usuario.setCorreoConfirmado(1);
        usuario.setVigenciaToken(null);
        usuario.setTokenType(null);
        usuario.setToken(null);
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(username);
        
        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Activar cuenta de usuario", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void deshabilitarCuenta(Long idUsuario, String userName) {
        // Encontrar el usuario o lanzar excepción si no se encuentra
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        // Actualizar estado y detalles de modificación
        usuario.setEstatus(EstatusUsuario.BLOQUEADO);
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(userName);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Deshabilitar cuenta de usuario", usuarioAnterior, usuario, userName);

        // Guardar cambios
        usuarioRepository.save(usuario);
    }


    @Override
    @Transactional
    public void habilitarCuenta(Long idUsuario, String userName) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);

        if(usuarioOptional.isEmpty())
            throw new NotFoundException("Usuario no encontrado");

        Usuario usuario = usuarioOptional.get();

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        usuario.setEstatus(EstatusUsuario.ACTIVO);
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(userName);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Habilitar cuenta de usuario", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario restablecerPassword(Long idUsuario, String userName) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);

        if(usuarioOptional.isEmpty())
            throw new NotFoundException("Usuario no encontrado");

        Usuario usuario = usuarioOptional.get();

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        // Generar token para validar email
        generarTokenUsuario(usuario, "Restablecer contraseña");
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(userName);
        usuario.setContrasenia(Strings.EMPTY);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Restablecer contraseña", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario olvidasteTuPassword(String correoElectronico) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(correoElectronico);

        if(usuarioOptional.isEmpty())
            throw new NotFoundException("Usuario no encontrado");

        Usuario usuario = usuarioOptional.get();

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        // Generar token para validar email
        generarTokenUsuario(usuario, "Olvidaste tu contraseña");
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(correoElectronico);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Olvidaste tu contraseña", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void asignarNuevoPassword(Usuario usuario) {

        String password = usuario.getContrasenia();
        String passwordEncrypted = passwordEncoder.encode(password);

        usuario = this.findById(usuario.getIdUsuario()).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String username = usuario.getModificadoPor();

        // Guardar información previa para la bitácora
        Usuario usuarioAnterior = new Usuario(usuario); // Hacer una copia del objeto para la bitácora

        // Se modifica el usuario
        usuario.setContrasenia(passwordEncrypted);
        usuario.setVigenciaToken(null);
        usuario.setTokenType(null);
        usuario.setToken(null);
        usuario.setFechaModificacion(new Date());
        usuario.setModificadoPor(username);

        // Guardar bitácora
        bitacoraUsuarioService.guardarBitacora("Asignar nueva contraseña", usuarioAnterior,
                usuario, usuario.getModificadoPor());

        usuarioRepository.save(usuario);
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

    private void validarActualizacionUsuario(Usuario usuario) {
        // Validar si ya existe un usuario que no este borrado y tenga el mismo nombre de usuario
        // y que no se trate de este mismo registro
        boolean alreadyExistsByUsuario = usuarioRepository
                .existsByEstatusNotAndUsuario(EstatusUsuario.BORRADO,
                        usuario.getUsuario(), usuario.getIdUsuario());

        if(alreadyExistsByUsuario)
            throw new BadRequestException("Ya existe un usuario con ese nombre de usuario");

        // Validar si ya existe un usuario que no este borrado y tenga el mismo email
        // y que no se trate de este mismo registro
        boolean alreadyExistsByEmail = usuarioRepository
                .existsByEstatusNotAndEmail(EstatusUsuario.BORRADO,
                        usuario.getEmail(), usuario.getIdUsuario());

        if(alreadyExistsByEmail)
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico");
    }

    private void generarTokenUsuario(Usuario usuario, String tokenType) {
        String tokenActivacion = passwordEncoder.encode(usuario.getNombre());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date()); // Hoy
        calendar.add(Calendar.DATE, 3);

        usuario.setToken(tokenActivacion);
        usuario.setVigenciaToken(calendar.getTime());
        usuario.setTokenType(tokenType);
    }


    private void validarCampos(Usuario usuario) {
        if(usuario.getUsuario() != null)
            ReplaceSymbolsUtil.replaceReservedWordsAndSymbols(usuario.getUsuario());

        if(usuario.getNombre() != null)
            ReplaceSymbolsUtil.replaceReservedWordsAndSymbols(usuario.getNombre());

        if(usuario.getEmail() != null)
            ReplaceSymbolsUtil.replaceReservedWordsAndSymbols(usuario.getEmail());

    }

}
