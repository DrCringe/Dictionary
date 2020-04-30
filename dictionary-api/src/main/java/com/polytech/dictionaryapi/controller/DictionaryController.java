package com.polytech.dictionaryapi.controller;

import com.polytech.dictionaryapi.exception.BadResourceException;
import com.polytech.dictionaryapi.exception.ResourceAlreadyExistsException;
import com.polytech.dictionaryapi.exception.ResourceNotFoundException;
import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class DictionaryController {

    private static final int ENTRIES_PER_PAGE = 10;

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping(value = "/entries", produces = MediaType.APPLICATION_JSON_VALUE)
    public  ResponseEntity<Page<Entry>> getAllEntries(@RequestParam(value = "page", defaultValue = "0") int pageNumber) {
        return ResponseEntity.ok(dictionaryService.findAll(pageNumber, ENTRIES_PER_PAGE));
    }

    @GetMapping(value = "/entries/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Entry> getEntryById(@PathVariable Long id) throws ResourceNotFoundException {
        Entry responseEntry = dictionaryService.findById(id);
        return ResponseEntity.ok(responseEntry);
    }

    @GetMapping(value = "/entries/letter/{letter}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Entry>> getEntriesByLetter(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @PathVariable char letter) {
        Page<Entry> responseEntries = dictionaryService.findByLetter(letter, pageNumber, ENTRIES_PER_PAGE);
        return ResponseEntity.ok(responseEntries);
    }

    @GetMapping(value = "/entries/word", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<?>> getEntriesByWord(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam String word) {
        try {
            Page<Entry> responseEntries = dictionaryService.findByWord(word, pageNumber, ENTRIES_PER_PAGE);
            return ResponseEntity.ok(responseEntries);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Message", ex.getMessage()).body(dictionaryService.getFuzzyEntries(word));
        }
    }

    @GetMapping(value = "/entries/type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Entry>> getEntriesByType(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam String type) throws ResourceNotFoundException {
        Page<Entry> responseEntries = dictionaryService.findByType(type, pageNumber, ENTRIES_PER_PAGE);
        return ResponseEntity.ok(responseEntries);
    }

    @GetMapping(value = "/entries/word+type", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Entry>> getEntriesByWordAndType(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam String word,
            @RequestParam String type) throws ResourceNotFoundException {
        Page<Entry> responseEntries = dictionaryService.findByWordAndType(word, type, pageNumber, ENTRIES_PER_PAGE);
        return ResponseEntity.ok(responseEntries);
    }

    @GetMapping(value = "/entries/search/{input}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getSearchSuggestions(@PathVariable String input) {
        return ResponseEntity.ok(dictionaryService.getSuggestions(input));
    }

    @PostMapping(value = "/entries")
    public ResponseEntity<Entry> addEntry(@Valid @RequestBody Entry entry)
            throws BadResourceException, ResourceAlreadyExistsException, URISyntaxException {
        Entry newEntry = dictionaryService.addNewEntry(entry);
        return ResponseEntity.created(new URI("/entries/" + newEntry.getId())).body(newEntry);
    }

    @PutMapping(value = "/entries/{id}")
    public ResponseEntity<Void> updateEntry(@PathVariable Long id, @Valid @RequestBody Entry updatedEntry)
            throws BadResourceException, ResourceNotFoundException {
        updatedEntry.setId(id);
        dictionaryService.updateEntry(updatedEntry);
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/entries/{id}")
    public ResponseEntity<Void> updateEntryDefinition(@PathVariable Long id, @RequestParam String newDefinition)
            throws BadResourceException, ResourceNotFoundException {
        dictionaryService.updateDefinitionById(id, newDefinition);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/entries/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) throws ResourceNotFoundException {
        dictionaryService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
