package com.polytech.dictionaryapi.specification;

import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.model.Entry_;
import org.springframework.data.jpa.domain.Specification;

public class EntrySpecification {

    public static Specification<Entry> getEntriesByWord(String word) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get(Entry_.word)), word.toLowerCase());
    }

    public static Specification<Entry> getEntriesByType(String type) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.lower(root.get(Entry_.wordtype)), type.toLowerCase());
    }

    public static Specification<Entry> getEntriesByLetter(char letter) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get(Entry_.word)), Character.toLowerCase(letter) + "%");
    }

    public static Specification<Entry> orderByWord() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.asc(root.get(Entry_.word)));
            return null;
        };
    }

}
