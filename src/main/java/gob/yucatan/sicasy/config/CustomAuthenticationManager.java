package gob.yucatan.sicasy.config;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {

    private final IUsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String presentedPassword = (String) authentication.getCredentials();

        Optional<Usuario> usuarioOptional = usuarioService.findByUsuario(username);

        if(usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            if(!passwordEncoder.matches(presentedPassword, usuario.getPassword()))
                throw new BadCredentialsException("InvalidCredentials");

            if(!usuario.isEnabled())
                throw new BadCredentialsException("UserAccountDisabled");

            log.info("Login success! - User: {}", usuario.getUsername());
            return createUsernamePasswordAuthenticationToken(usuario);
        }
        else {
            throw new BadCredentialsException("UserAccountNotExists");
        }
    }

    private UsernamePasswordAuthenticationToken createUsernamePasswordAuthenticationToken(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                        userDetails.getPassword(),
                        userDetails.getAuthorities());
        authenticationToken.setDetails(userDetails);
        return authenticationToken;
    }
}
