package com.polytech.dictionaryapi.service;

import com.polytech.dictionaryapi.exception.BadResourceException;
import com.polytech.dictionaryapi.exception.ResourceAlreadyExistsException;
import com.polytech.dictionaryapi.exception.ResourceNotFoundException;
import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.repository.EntryRepository;
import com.polytech.dictionaryapi.specification.EntrySpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DictionaryServiceUnitTests {

    private static Entry testEntry = new Entry();

    @Mock
    private EntryRepository mockedEntryRepository;

    @InjectMocks
    private static DictionaryService dictionaryService;

    @BeforeAll
    public void initCriteriaAPIInterfaces() {
        testEntry.setId(1234567L);
        testEntry.setWord("TestEntry");
        testEntry.setWordtype("test.");
        testEntry.setDefinition("Test definition");

        HashMap<String, Integer> testDistinctWords = new HashMap<>();
        testDistinctWords.put(testEntry.getWord(), 1);
        ReflectionTestUtils.setField(dictionaryService, "distinctWords", testDistinctWords);
    }

    @BeforeEach
    public void setUp() {
        testEntry = new Entry();
        testEntry.setId(1234567L);
        testEntry.setWord("TestEntry");
        testEntry.setWordtype("test.");
        testEntry.setDefinition("Test definition");
    }

    @Test
    public void whenFindById_thenReturnEntry() {
        when(mockedEntryRepository.existsById(1234567L)).thenReturn(true);
        when(mockedEntryRepository.findById(1234567L)).thenReturn(Optional.ofNullable(testEntry));

        Entry foundEntry = dictionaryService.findById(1234567L);
        assertEquals(testEntry, foundEntry);
    }

    @Test
    public void givenEntry_whenAddValidEntry_thenSaveEntry() {
        when(mockedEntryRepository.save(testEntry)).thenReturn(testEntry);

        Entry savedEntry = dictionaryService.addNewEntry(testEntry);
        assertEquals(testEntry, savedEntry);
    }

    @Test
    public void whenDeletedById_thenNotFound() {
        when(mockedEntryRepository.existsById(1234567L)).thenReturn(true);
        when(mockedEntryRepository.findById(1234567L)).thenReturn(Optional.ofNullable(testEntry));
        dictionaryService.deleteById(testEntry.getId());

        when(mockedEntryRepository.existsById(1234567L)).thenReturn(false);
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.findById(testEntry.getId()));
        assertEquals("Entry not found with id \"1234567\"", exception.getMessage());


        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Entry> emptyPage = new PageImpl<>(Collections.emptyList());
        ArgumentCaptor<EntrySpecification> entrySpecificationArgumentCaptor = ArgumentCaptor.forClass(EntrySpecification.class);

        when(mockedEntryRepository.findAll((Specification<Entry>) entrySpecificationArgumentCaptor.capture(), ArgumentMatchers.eq(pageRequest))).thenReturn(emptyPage);
        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.findByWord("TestEntry", 0, 10));
        assertEquals("Entries not found with word \"TestEntry\"", exception.getMessage());

        when(mockedEntryRepository.findAll((Specification<Entry>) entrySpecificationArgumentCaptor.capture(), ArgumentMatchers.eq(pageRequest))).thenReturn(emptyPage);
        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.findByType("test.", 0, 10));
        assertEquals("Entries not found with type \"test.\"", exception.getMessage());

        when(mockedEntryRepository.findAll((Specification<Entry>) entrySpecificationArgumentCaptor.capture(), ArgumentMatchers.eq(pageRequest))).thenReturn(emptyPage);
        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.findByWordAndType("TestEntry", "test.", 0, 10));
        assertEquals("Entries not found with word \"TestEntry\" and type \"test.\"", exception.getMessage());

        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.deleteById(testEntry.getId()));
        assertEquals("Entry not found with id \"1234567\"", exception.getMessage());

        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.updateEntry(testEntry));
        assertEquals("Entry not found with id \"1234567\"", exception.getMessage());

        exception = assertThrows(ResourceNotFoundException.class, () -> dictionaryService.updateDefinitionById(testEntry.getId(), "Test definition"));
        assertEquals("Entry not found with id \"1234567\"", exception.getMessage());
    }

    @Test
    public void whenAddNewInvalidEntry_thenBadResource() {
        ArgumentCaptor<EntrySpecification> entrySpecificationArgumentCaptor = ArgumentCaptor.forClass(EntrySpecification.class);
        when(mockedEntryRepository.findAll((Specification<Entry>) entrySpecificationArgumentCaptor.capture())).thenReturn(Collections.singletonList(testEntry));

        Exception exception = assertThrows(ResourceAlreadyExistsException.class, () -> dictionaryService.addNewEntry(testEntry));
        assertEquals("Entry for word \"TestEntry\" with specified definition already exists", exception.getMessage());

        testEntry.setDefinition("");
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.addNewEntry(testEntry));
        assertEquals("Entry has empty word, type or definition", exception.getMessage());

        testEntry.setWordtype(null);
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.addNewEntry(testEntry));
        assertEquals("Entry has NULL word, type or definition", exception.getMessage());

        testEntry = null;
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.addNewEntry(testEntry));
        assertEquals("Entry is NULL", exception.getMessage());
    }

    @Test
    public void whenUpdateInvalidEntry_thenBadResource() {
        testEntry.setDefinition("");
        Exception exception = assertThrows(BadResourceException.class, () -> dictionaryService.updateEntry(testEntry));
        assertEquals("Entry has empty word, type or definition", exception.getMessage());

        testEntry.setWordtype(null);
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.updateEntry(testEntry));
        assertEquals("Entry has NULL word, type or definition", exception.getMessage());

        testEntry = null;
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.updateEntry(testEntry));
        assertEquals("Entry is NULL", exception.getMessage());
    }

    @Test
    public void whenUpdateInvalidDefinition_thenBadResource() {
        testEntry.setDefinition("");
        Exception exception = assertThrows(BadResourceException.class, () -> dictionaryService.updateDefinitionById(testEntry.getId(), ""));
        assertEquals("Definition is empty", exception.getMessage());

        testEntry.setDefinition(null);
        exception = assertThrows(BadResourceException.class, () -> dictionaryService.updateDefinitionById(testEntry.getId(), null));
        assertEquals("Definition is NULL", exception.getMessage());
    }

    @Test
    public void whenFuzzySearchRequest_thenContainsResult() {
        testEntry.setWord("Spring");

        when(mockedEntryRepository.save(testEntry)).thenReturn(testEntry);

        dictionaryService.addNewEntry(testEntry);
        assertTrue(dictionaryService.getFuzzyEntries("Sprnig").getContent().contains("Spring"));
    }

}
