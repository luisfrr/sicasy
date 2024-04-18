package gob.yucatan.sicasy.services.iface;

import gob.yucatan.sicasy.business.entities.EmailTemplate;

import java.util.Optional;

public interface IEmailTemplateService {

    Optional<EmailTemplate> getEmailTemplate(String codigo);

}
