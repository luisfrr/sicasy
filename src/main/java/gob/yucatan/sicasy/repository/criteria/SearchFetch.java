package gob.yucatan.sicasy.repository.criteria;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;

public class SearchFetch {

    @Getter
    private String field;

    @Setter
    @Getter
    private JoinType joinType;

    @Setter
    @Getter
    private List<String> fieldPath;

    private boolean subField;

    public SearchFetch(JoinType joinType, String... field) {
        this.setJoinType(joinType);
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
