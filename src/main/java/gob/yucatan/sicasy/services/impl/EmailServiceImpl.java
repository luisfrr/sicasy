package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.EmailTemplateMessage;
import gob.yucatan.sicasy.business.entities.EmailTemplate;
import gob.yucatan.sicasy.business.entities.Usuario;
import gob.yucatan.sicasy.business.enums.EmailTemplateEnum;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.services.iface.IEmailService;
import gob.yucatan.sicasy.services.iface.IEmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    @Value("${app.url}")
    private String appUrl;

    private final JavaMailSender emailSender;
    private final IEmailTemplateService emailTemplateService;

    @Override
    public void sendEmail(String to, String subject, String body) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setTo(to);
            helper.setFrom("sicasy-no-reply@outlook.com");
            helper.setSubject(subject);
            helper.setText(body, true); // true para habilitar HTML

            emailSender.send(message);
            log.info("Correo enviado correctamente");
        } catch (MessagingException e) {
            log.error("No se ha logrado enviar el correo.");
        }
    }

    @Override
    public void sendMail(EmailTemplateMessage templateMessage) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String body = buildBodyMessage(templateMessage.getEmailTemplate(), templateMessage.getDataTemplate());

            helper.setTo(templateMessage.getTo());
            helper.setFrom("sicasy-no-reply@outlook.com");
            helper.setSubject(templateMessage.getSubject());
            helper.setText(body, true); // true para habilitar HTML

            emailSender.send(message);
            log.info("Correo enviado correctamente");
        } catch (MessagingException e) {
            log.error("No se ha logrado enviar el correo.");
        }
    }

    @Override
    public void sendActivateAccountEmail(Usuario usuario) {
        String activateUrl = appUrl + "/views/auth/activate.faces?token=" + usuario.getToken();

        Map<String, String> dataTemplate = getSimpleTemplateData("¡Registro exitoso!", """
                Necesitamos validar su dirección de correo electrónico y debe asignar una contraseña para activar su cuenta. <br/>
                Haga clic en el siguiente botón para continuar con la activación.
                """, "ACTIVAR CUENTA", activateUrl);

        this.sendMail(EmailTemplateMessage.builder()
                .emailTemplate(EmailTemplateEnum.SIMPLE_TEMPLATE)
                .to(usuario.getEmail())
                .subject("SICASY - ¡Registro exitoso!")
                .dataTemplate(dataTemplate)
                .build());
    }

    @Override
    public void sendResetPasswordEmail(Usuario usuario) {
        String url = appUrl + "/views/auth/resetpassword.faces?token=" + usuario.getToken();

        Map<String, String> dataTemplate = getSimpleTemplateData("Restablecer contraseña", """
                Para concluir con el proceso de restablecer tu contraseña, tendrás que crear una nueva contraseña para ingresar a tu cuenta SICASY.
                Haga clic en el siguiente botón para asignar una nueva contraseña.
                """, "NUEVA CONTRASEÑA", url);

        this.sendMail(EmailTemplateMessage.builder()
                .emailTemplate(EmailTemplateEnum.SIMPLE_TEMPLATE)
                .to(usuario.getEmail())
                .subject("SICASY - Restablecer contraseña")
                .dataTemplate(dataTemplate)
                .build());
    }

    @Override
    public void sendForgotPasswordEmail(Usuario usuario) {
        String url = appUrl + "/views/auth/resetpassword.faces?token=" + usuario.getToken();

        Map<String, String> dataTemplate = getSimpleTemplateData("¿Olvidaste tu contraseña?", """
                Usted recibió este correo electrónico, porque fue solicitado por un usuario de SICASY. <br/>
                Esto es parte del procedimiento para crear una nueva contraseña en el sistema. <br/>
                Si NO lo solicitó, ignore este correo electrónico, su contraseña seguirá siendo la misma.
                """, "NUEVA CONTRASEÑA", url);

        this.sendMail(EmailTemplateMessage.builder()
                .emailTemplate(EmailTemplateEnum.SIMPLE_TEMPLATE)
                .to(usuario.getEmail())
                .subject("SICASY - ¿Olvidaste tu contraseña?")
                .dataTemplate(dataTemplate)
                .build());
    }

    private static Map<String, String> getSimpleTemplateData(String title, String description, String actionText, String actionUrl) {
        Map<String, String> dataTemplate = new HashMap<>();
        dataTemplate.put("#EMAIL_TITLE#", title);
        dataTemplate.put("#EMAIL_DESCRIPTION#", description);
        dataTemplate.put("#ACTION_TEXT#", actionText);
        dataTemplate.put("#ACTION_URL#", actionUrl);
        return dataTemplate;
    }

    private String buildBodyMessage(EmailTemplateEnum emailTemplate, Map<String, String> dataTemplate) {

        Optional<EmailTemplate> emailTemplateOptional =
                emailTemplateService.getEmailTemplate(emailTemplate.getCodigo());

        if(emailTemplateOptional.isEmpty())
            throw new BadRequestException("No se ha encontrado el email template.");

        String body = emailTemplateOptional.get().getTemplate();

        for(String key : dataTemplate.keySet()) {
            body = body.replace(key, dataTemplate.get(key));
        }

        return body;
    }
}
