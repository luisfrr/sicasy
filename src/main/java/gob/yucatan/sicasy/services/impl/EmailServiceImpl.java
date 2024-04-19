package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.dtos.EmailTemplateMessage;
import gob.yucatan.sicasy.business.entities.EmailTemplate;
import gob.yucatan.sicasy.business.enums.EmailTemplateEnum;
import gob.yucatan.sicasy.business.exceptions.BadRequestException;
import gob.yucatan.sicasy.services.iface.IEmailService;
import gob.yucatan.sicasy.services.iface.IEmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

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
