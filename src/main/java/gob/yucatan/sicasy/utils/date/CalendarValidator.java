package gob.yucatan.sicasy.utils.date;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@FacesValidator("calendarValidator")
public class CalendarValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        Date date2 = (Date) value;
        System.out.println( ((UIInput) context.getViewRoot().findComponent(
                "form_dialog:id_calendar1")).getValue() );
        log.info("validando fechas" );
        if (validateDate(date2)) {
            throw new ValidatorException(new FacesMessage("A valid date"));
        } else {
            throw new ValidatorException(new FacesMessage("date dont figure in the database"));
        }

    }
    
    private boolean validateDate(Date date) {
        return true;
    }
}
