package gob.yucatan.sicasy.views.auth;

import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.services.iface.IUsuarioService;
import gob.yucatan.sicasy.views.beans.Messages;
import gob.yucatan.sicasy.views.beans.UserSessionBean;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
@Scope("view")
@RequiredArgsConstructor
@Slf4j
public class ActivateView implements Serializable {

    private @Getter String title;
    private Usuario usuarioSelected;

    private @Getter @Setter String password;
    private @Getter @Setter String confirmPassword;
    private @Getter boolean showReturnToLogin = false;
    private @Getter boolean showFormPassword = false;

    private final UserSessionBean userSessionBean;
    private final IUsuarioService usuarioService;

    @PostConstruct
    public void init() {
        log.info("ActivateView init");
        title = "Activación de cuenta";

        // Obtener el token del parámetro de la URL
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String tokenParam = facesContext.getExternalContext().getRequestParameterMap().get("token");

        // Aquí puedes utilizar el token para activar la cuenta
        if (tokenParam != null && !tokenParam.isEmpty()) {
            Optional<Usuario> usuarioOptional = usuarioService.findByToken(tokenParam);

            if(usuarioOptional.isPresent()) {
                usuarioSelected = usuarioOptional.get();

                String tokenType = usuarioSelected.getTokenType();
                Date vigenciaToken = usuarioSelected.getVigenciaToken();
                Date ahora = new Date();

                // Comparar la fecha actual con la fecha de vigencia del token
                if (Objects.equals(tokenType, "Activación") && vigenciaToken != null && vigenciaToken.after(ahora)) {
                    showFormPassword =true;
                } else {
                    Messages.addError("El enlace ha caducado o no es válido.");
                    showReturnToLogin = true;
                }

            } else {
                Messages.addError("El enlace no es válido.");
                showReturnToLogin = true;
            }
        } else {
            Messages.addError("El enlace no es válido.");
            showReturnToLogin = true;
        }
    }

    public void activarCuenta() {
        try {
            if(password == null || confirmPassword == null)
                throw new BadRequestException("Contraseña y confirmar contraseña son campos obligatorios.");

            if(!password.equals(confirmPassword))
                throw new BadRequestException("Las contraseñas no coinciden.");

            usuarioSelected.setModificadoPor(userSessionBean.getUserName());

            usuarioSelected.setContrasenia(confirmPassword);
            usuarioService.activarCuenta(usuarioSelected);

            showFormPassword = false;
            showReturnToLogin = true;

            Messages.addInfo("Tu cuenta se ha activado correctamente. Ahora puedes iniciar sesión.");

        } catch (Exception e) {
            Messages.addError(e.getMessage());
        }

    }

    public String returnToLogin() {
        SecurityContextHolder.clearContext();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login?faces-redirect=true";
    }
}
