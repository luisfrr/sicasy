package gob.yucatan.sicasy.utils.date;

import java.util.Date;

public class DateValidator {

    public static boolean isDateBetween(Date startDate, Date endDate, Date dateToValid) {
        try {
            return !dateToValid.before(startDate) && !dateToValid.after(endDate);
        } catch (Exception e) {
            return false;
        }
    }

}
