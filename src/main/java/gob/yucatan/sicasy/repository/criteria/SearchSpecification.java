package gob.yucatan.sicasy.repository.criteria;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SearchSpecification<T> implements Specification<T> {

    private final List<SearchCriteria> searchCriteriaList;
    private final List<SearchFetch> searchFetchList;

    @Getter
    @Setter
    private Boolean selectDistinct;

    public SearchSpecification() {
        this.searchCriteriaList = new ArrayList<>();
        this.searchFetchList = new ArrayList<>();
        this.selectDistinct = false;
    }

    public void add(SearchCriteria criteria) {
        searchCriteriaList.add(criteria);
    }

    public void add(SearchFetch fetch) {
        searchFetchList.add(fetch);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        for (SearchCriteria criteria : searchCriteriaList) {

            Path<?> path = null;

            if(criteria.isSubField()) {
                List<String> fieldPath = criteria.getFieldPath();

                int count = 0;
                for (String field : fieldPath) {
                    if(count == 0)
                        path = root.get(field);
                    else
                        path = path.get(field);

                    count++;
                }
            }

            switch (criteria.getSearchOperation() ) {
                case GREATER_THAN:
                    predicates.add(criteriaBuilder.greaterThan(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue().toString()));
                    break;
                case LESS_THAN:
                    predicates.add(criteriaBuilder.lessThan(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue().toString()));
                    break;
                case GREATER_THAN_EQUAL:
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue().toString()));
                    break;
                case LESS_THAN_EQUAL:
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue().toString()));
                    break;
                case NOT_EQUAL:
                    predicates.add(criteriaBuilder.notEqual(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue()));
                    break;
                case EQUAL:
                    predicates.add(criteriaBuilder.equal(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()),
                            criteria.getValue()));
                    break;
                case MATCH:
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField())),
                            "%" + criteria.getValue().toString().toLowerCase() + "%"));
                    break;
                case MATCH_END:
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField())),
                            criteria.getValue().toString().toLowerCase() + "%"));
                    break;
                case MATCH_START:
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField())),
                            "%" + criteria.getValue().toString().toLowerCase()));
                    break;
                case IN:
                    predicates.add(criteriaBuilder.in(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()))
                            .value(criteria.getValue()));
                    break;
                case NOT_IN:
                    predicates.add(criteriaBuilder.in(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField()))
                                    .value(criteria.getValue()).not());
                    break;
                case IS_NOT_NULL:
                    predicates.add(criteriaBuilder.isNotNull(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField())));
                    break;
                case IS_NULL:
                    predicates.add(criteriaBuilder.isNull(criteria.isSubField() ? Objects.requireNonNull(path).get(criteria.getField()) : root.get(criteria.getField())));
                    break;

            }
        }

        for (SearchFetch searchFetch : searchFetchList) {

            FetchParent<?, ?> fetchParent = root;

            if(searchFetch.isSubField()) {
                List<String> fieldPath = searchFetch.getFieldPath();

                int count = 0;
                for (String field : fieldPath) {
                    if(count == 0)
                        fetchParent = root.fetch(field, searchFetch.getJoinType());
                    else
                        fetchParent = fetchParent.fetch(field, searchFetch.getJoinType());

                    count++;
                }

                fetchParent.fetch(searchFetch.getField(), searchFetch.getJoinType());

            } else {
                root.fetch(searchFetch.getField(), searchFetch.getJoinType());
            }

        }

        if(this.getSelectDistinct()) {
            query.distinct(true);
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
