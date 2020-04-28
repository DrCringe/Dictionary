package com.polytech.dictionaryapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polytech.dictionaryapi.exception.ResourceNotFoundException;
import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.service.DictionaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DictionaryController.class)
public class DictionaryControllerUnitTests {

    private static final Entry TEST_ENTRY = new Entry();

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DictionaryService dictionaryService;

    public static String asJsonString(final Object obj) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    @BeforeEach
    public void setUp() {
        TEST_ENTRY.setId(1234567L);
        TEST_ENTRY.setWord("TestEntry");
        TEST_ENTRY.setWordtype("test.");
        TEST_ENTRY.setDefinition("Test definition");
    }

    @Test
    public void givenEntries_whenRequestedAllEntries_returnJsonArray() throws Exception {
        List<Entry> testEntryList = Arrays.asList(new Entry(), TEST_ENTRY, new Entry());

       when(dictionaryService.findAll(0, 10))
                .thenReturn(new PageImpl<>(testEntryList, PageRequest.of(0, 3), 3));

        mvc.perform(get("/entries")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[1].word", is("TestEntry")));
    }

    @Test
    public void givenEntries_whenRequestedExistentWord_returnJsonArray() throws Exception {
        List<Entry> testEntryList = Collections.singletonList(TEST_ENTRY);

        when(dictionaryService.findByWord("TestEntry", 0, 10))
                .thenReturn(new PageImpl<>(testEntryList, PageRequest.of(0, 1), 1));

        mvc.perform(get("/entries/word?word=TestEntry")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].word", is("TestEntry")));
    }

    @Test
    public void givenEntries_whenRequestedNonexistentWord_returnNotFoundAndJsonArray() throws Exception {
        List<Entry> testEntryList = Collections.singletonList(TEST_ENTRY);

        when(dictionaryService.findByWord("NonexistentTestEntry", 0, 10))
                .thenThrow(new ResourceNotFoundException("Entry not found with word \"NonexistentTestEntry\""))
                .thenReturn(new PageImpl<>(testEntryList, PageRequest.of(0, 1), 1));

        mvc.perform(get("/entries/word?word=NonexistentTestEntry")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound());
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.content[0].word", is("TestEntry")));

        mvc.perform(get("/entries/word?word=NonexistentTestEntry")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].word", is("TestEntry")));
    }

    @Test
    public void givenEntries_whenRequestedType_returnJsonArray() throws Exception {
        List<Entry> testEntriesList = Arrays.asList(new Entry(), TEST_ENTRY, new Entry());
        testEntriesList.get(0).setWordtype("test.");
        testEntriesList.get(2).setWordtype("test.");

        when(dictionaryService.findByType("test.", 0, 10))
                .thenReturn(new PageImpl<>(testEntriesList, PageRequest.of(0, 3), 3));

        mvc.perform(get("/entries/type?type=test.")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[1].word", is("TestEntry")));
    }

    @Test
    public void givenEntries_whenRequestedWordAndType_returnJsonArray() throws Exception {
        List<Entry> testEntriesList = Arrays.asList(TEST_ENTRY, new Entry());
        testEntriesList.get(1).setWord("TestEntry");
        testEntriesList.get(1).setWordtype("test.");
        testEntriesList.get(1).setDefinition("Another test definition");

        when(dictionaryService.findByWordAndType("TestEntry", "test.", 0, 10))
                .thenReturn(new PageImpl<>(testEntriesList, PageRequest.of(0, 2), 2));

        mvc.perform(get("/entries/word+type?word=TestEntry&type=test.")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].definition", is("Test definition")))
                .andExpect(jsonPath("$.content[1].definition", is("Another test definition")));
    }

    @Test
    public void givenEntry_whenRequestedEntryAdd_returnJsonValue() throws Exception {
        when(dictionaryService.addNewEntry(TEST_ENTRY))
                .thenReturn(TEST_ENTRY);

        mvc.perform(post("/entries")
                .content(asJsonString(TEST_ENTRY))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.word", is("TestEntry")));
    }

    @Test
    public void givenEntry_whenRequestedEntryUpdate_returnVoidOk() throws Exception {
        TEST_ENTRY.setWord("UpdatedWord");
        TEST_ENTRY.setWordtype("updatedWordType.");
        TEST_ENTRY.setDefinition("Updated test definition");

        doReturn(ResponseEntity.ok().build())
                .when(mock(DictionaryController.class))
                .updateEntry(1234567L, TEST_ENTRY);

        mvc.perform(put("/entries/1234567")
                .content(asJsonString(TEST_ENTRY))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void givenEntry_whenRequestedDefinitionUpdate_returnVoidOk() throws Exception {
        doReturn(ResponseEntity.ok().build())
                .when(mock(DictionaryController.class))
                .updateEntryDefinition(1234567L, "Updated test definition");

        mvc.perform(patch("/entries/1234567")
                .content(asJsonString("Updated test definition"))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void givenEntry_whenRequestedEntryDelete_returnVoidOk() throws Exception {
        doReturn(ResponseEntity.ok().build())
                .when(mock(DictionaryController.class))
                .deleteEntry(1234567L);

        mvc.perform(delete("/entries/1234567"))
                .andExpect(status().isOk());
    }

}
