package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.EmailTemplateMessage;
import gob.yucatan.sicasy.business.entities.Usuario;

public interface IEmailService {

    void sendEmail(String to, String subject, String body);
    void sendMail(EmailTemplateMessage templateMessage);

    void sendActivateAccountEmail(Usuario usuario);
    void sendResetPasswordEmail(Usuario usuario);

}
