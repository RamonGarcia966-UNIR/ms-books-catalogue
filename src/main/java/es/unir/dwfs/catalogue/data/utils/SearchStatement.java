package es.unir.dwfs.catalogue.data.utils;

import es.unir.dwfs.catalogue.data.model.Book;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Especificación JPA para construir queries dinámicas
 */
@AllArgsConstructor
public class SearchStatement implements Specification<Book> {

    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        switch (criteria.getOperation()) {
            case EQUAL:
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case LIKE:
                return builder.like(
                        builder.lower(root.get(criteria.getKey())),
                        "%" + criteria.getValue().toString().toLowerCase() + "%");
            case IN:
                return builder.in(root.get(criteria.getKey())).value(criteria.getValue());
            case GREATER_THAN:
                return builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN:
                return builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            default:
                return null;
        }
    }
}
