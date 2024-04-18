package gob.yucatan.sicasy.services.impl;

import gob.yucatan.sicasy.business.entities.EmailTemplate;
import gob.yucatan.sicasy.repository.iface.IEmailTemplateRepository;
import gob.yucatan.sicasy.services.iface.IEmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements IEmailTemplateService {

    private final IEmailTemplateRepository emailTemplateRepository;

    @Override
    public Optional<EmailTemplate> getEmailTemplate(String codigo) {
        return emailTemplateRepository.findByCodigo(codigo);
    }
}
