package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.dtos.EmailTemplateMessage;

public interface IEmailService {

    void sendEmail(String to, String subject, String body);
    void sendMail(EmailTemplateMessage templateMessage);

}
