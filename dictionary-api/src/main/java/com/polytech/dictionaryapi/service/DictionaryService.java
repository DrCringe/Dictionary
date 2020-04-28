package com.polytech.dictionaryapi.service;

import com.polytech.dictionaryapi.exception.BadResourceException;
import com.polytech.dictionaryapi.exception.ResourceAlreadyExistsException;
import com.polytech.dictionaryapi.exception.ResourceNotFoundException;
import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.model.Entry_;
import com.polytech.dictionaryapi.repository.EntryRepository;
import com.polytech.dictionaryapi.specification.EntrySpecification;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictionaryService {
    /*
     *  distinctWords is a collection of pairs {unique_word, amount_of_entries_in_database_with_this_word}
     *  it helps to avoid unnecessary DB queries in fuzzy search and live search implementations
     *  it's getting filled on 'ApplicationIsReady' event and every method that modifying DB also modifies it
     *  in this example with relatively small DB it collects all unique words, but with larger dictionaries
     *  it's recommended to keep only most relevant entries in it to avoid performance issues
     *  P.S. It supposed to be implemented with Spring Caching, but never got time for it :(
     */
    private HashMap<String, Integer> distinctWords = null;

    @Autowired
    private EntryRepository entryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private boolean existsById(Long id) {
        return entryRepository.existsById(id);
    }

    public Entry findById(Long id) throws ResourceNotFoundException {
        if (existsById(id)) {
            return entryRepository.findById(id).get();
        } else {
            throw new ResourceNotFoundException("Entry not found with id \"" + id + "\"");
        }
    }

    public void deleteById(Long id) throws ResourceNotFoundException {
        if (existsById(id)) {
            distinctWords.compute(findById(id).getWord(), (key, value) -> value == 1 ? null : --value);
            entryRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Entry not found with id \"" + id + "\"");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    private void getDistinctWords() {
        if (entityManager == null) return;  //  workaround to prevent NullPointerException when ApplicationReadyEvent
                                            //  is published during controller @WebMvcTest initialization

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> criteriaQuery = criteriaBuilder.createQuery(Object[].class);
        Root<Entry> entryRoot = criteriaQuery.from(Entry.class);
        criteriaQuery.groupBy(entryRoot.get(Entry_.word));
        criteriaQuery.multiselect(entryRoot.get(Entry_.word), criteriaBuilder.count(entryRoot));
        TypedQuery<Object[]> typedQuery = entityManager.createQuery(criteriaQuery);

        this.distinctWords = typedQuery.getResultList().stream()    //  using getResultsStream() here throwing PSQLException 'This ResultSet is closed'
                                                                    //  adding @Transactional annotation to method signature workaround doesn't fix it,
                                                                    //  apparently query connection is closed before .collect() initiates traversal
                .map(item -> Pair.of(item[0].toString(), Integer.parseInt(item[1].toString())))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, Math::addExact, HashMap::new));
    }

    public Page<String> getFuzzyEntries(String word) throws ResourceNotFoundException {
        List<ExtractedResult> fuzzyWords = FuzzySearch.extractSorted(word, distinctWords.keySet(), 80);
        List<String> fuzzyList = fuzzyWords.stream()
                .map(ExtractedResult::getString)
                .filter(item -> item.length() > word.length() || item.length() > 5)
                .limit(30)
                .collect(Collectors.toList());
        if (fuzzyList.isEmpty()) throw new ResourceNotFoundException("No similar word found for request \"" + word + "\"");

        return new PageImpl<>(fuzzyList, PageRequest.of(0, fuzzyList.size()), fuzzyList.size());
    }

    public List<String> getSuggestions(String input) {
        return distinctWords.entrySet().stream()
                .filter(item -> item.getKey().startsWith(input))
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Page<Entry> findAll(int pageNumber, int perPage) {
        return entryRepository.findAll(EntrySpecification.orderByWord(), PageRequest.of(pageNumber, perPage));
    }

    public Page<Entry> findByLetter(char letter, int pageNumber, int perPage) {
        return entryRepository.findAll(Specification.where(EntrySpecification.getEntriesByLetter(letter)
                .and(EntrySpecification.orderByWord())), PageRequest.of(pageNumber, perPage));
    }

    public Page<Entry> findByWord(String word, int pageNumber, int perPage) throws ResourceNotFoundException {
        Page<Entry> wordEntries = entryRepository.findAll(EntrySpecification.getEntriesByWord(word),
                PageRequest.of(pageNumber, perPage));
        if (wordEntries.isEmpty()) {
            throw new ResourceNotFoundException("Entries not found with word \"" + word + "\"");
        } else {
            return wordEntries;
        }
    }

    private List<Entry> findAllByWord(String word) {
        return entryRepository.findAll(EntrySpecification.getEntriesByWord(word));
    }

    public Page<Entry> findByType(String type, int pageNumber, int perPage) throws ResourceNotFoundException {
        Page<Entry> typeEntries = entryRepository.findAll(Specification.where(EntrySpecification.getEntriesByType(type)
                .and(EntrySpecification.orderByWord())), PageRequest.of(pageNumber, perPage));
        if (typeEntries.isEmpty()) {
            throw new ResourceNotFoundException("Entries not found with type \"" + type +"\"");
        } else {
            return typeEntries;
        }
    }

    public Page<Entry> findByWordAndType(String word, String type, int pageNumber, int perPage) throws ResourceNotFoundException {
        Page<Entry> wtEntries = entryRepository.findAll(Specification.where(EntrySpecification.getEntriesByWord(word)
                .and(EntrySpecification.getEntriesByType(type)).and(EntrySpecification.orderByWord())), PageRequest.of(pageNumber, perPage));
        if (wtEntries.isEmpty()) {
            throw new ResourceNotFoundException("Entries not found with word \"" + word + "\" and type \"" + type +"\"");
        } else {
            return wtEntries;
        }
    }

    public Entry addNewEntry(Entry newEntry) throws BadResourceException, ResourceAlreadyExistsException {
        if (newEntry == null) {
            throw new BadResourceException("Entry is NULL");
        } else if (newEntry.getWord() == null || newEntry.getWordtype() == null || newEntry.getDefinition() == null) {
            throw new BadResourceException("Entry has NULL word, type or definition");
        } else if (newEntry.getWord().isEmpty() || newEntry.getWordtype().isEmpty() || newEntry.getDefinition().isEmpty()) {
            throw new BadResourceException("Entry has empty word, type or definition");
        } else if (findAllByWord(newEntry.getWord()).contains(newEntry)) {
            throw new ResourceAlreadyExistsException("Entry for word \"" + newEntry.getWord() + "\" with specified definition already exists");
        } else {
            distinctWords.merge(newEntry.getWord(), 1, Integer::sum);
            return entryRepository.save(newEntry);
        }
    }

    public void updateEntry(Entry updatedEntry) throws BadResourceException, ResourceNotFoundException {
        if (updatedEntry == null) {
            throw new BadResourceException("Entry is NULL");
        } else if (updatedEntry.getWord() == null || updatedEntry.getWordtype() == null || updatedEntry.getDefinition() == null) {
            throw new BadResourceException("Entry has NULL word, type or definition");
        } else if (updatedEntry.getWord().isEmpty() || updatedEntry.getWordtype().isEmpty() || updatedEntry.getDefinition().isEmpty()) {
            throw new BadResourceException("Entry has empty word, type or definition");
        } else if (!existsById(updatedEntry.getId())) {
            throw new ResourceNotFoundException("Entry not found with id \"" + updatedEntry.getId() + "\"");
        } else {
            if (!updatedEntry.getWord().equals(findById(updatedEntry.getId()).getWord())) {
                distinctWords.merge(updatedEntry.getWord(), 1, Integer::sum);
                distinctWords.compute(findById(updatedEntry.getId()).getWord(), (key, value) -> value == 1 ? null : --value);
            }
            entryRepository.save(updatedEntry);
        }
    }

    public void updateDefinitionById(Long id, String newDefinition) throws BadResourceException, ResourceNotFoundException {
        if (newDefinition == null) {
            throw new BadResourceException("Definition is NULL");
        } else if (newDefinition.isEmpty()) {
            throw new BadResourceException("Definition is empty");
        } else if (!existsById(id)) {
            throw new ResourceNotFoundException("Entry not found with id \"" + id + "\"");
        } else {
            Entry updatedEntry = entryRepository.findById(id).get();
            updatedEntry.setDefinition(newDefinition);
            entryRepository.save(updatedEntry);
        }
    }

}
