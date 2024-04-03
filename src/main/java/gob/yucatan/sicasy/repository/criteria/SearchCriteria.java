package gob.yucatan.sicasy.repository.criteria;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteria {

    @Getter
    private String field;

    @Setter
    @Getter
    private Object value;

    @Setter
    @Getter
    private SearchOperation searchOperation;

    @Setter
    @Getter
    private List<String> fieldPath;
    private boolean subField;

    public SearchCriteria(SearchOperation searchOperation, Object value, String... field) {
        this.setSearchOperation(searchOperation);
        this.setValue(value);
        this.setField(field);
    }

    private void setField(String... fields) {
        List<String> fieldPath = new ArrayList<>();

        int totalAttr = fields.length;

        if(totalAttr > 1)
            subField = true;

        // Se recorren todos los campos que se agregaron
        for(int index = 0; index < fields.length; index++) {

            if(index == (totalAttr - 1))
                this.field = fields[index];
            else
                fieldPath.add(fields[index]);
        }

        this.setFieldPath(fieldPath);
    }

    public String getCompleteFieldPath() {
        if(this.isSubField())
         return String.join(".", this.fieldPath) + "." + this.field;
        else
            return this.field;
    }

    public Boolean isSubField() {
        return this.subField;
    }

}
