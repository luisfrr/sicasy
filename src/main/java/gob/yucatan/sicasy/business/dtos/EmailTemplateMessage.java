package gob.yucatan.sicasy.business.dtos;

import gob.yucatan.sicasy.business.enums.EmailTemplateEnum;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EmailTemplateMessage {
    private String to;
    private String subject;
    private EmailTemplateEnum emailTemplate;
    private Map<String, String> dataTemplate;
}
