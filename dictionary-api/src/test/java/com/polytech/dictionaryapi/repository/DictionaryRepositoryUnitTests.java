package com.polytech.dictionaryapi.repository;

import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.specification.EntrySpecification;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = { DictionaryRepositoryUnitTests.Initializer.class })
public class DictionaryRepositoryUnitTests {

    private Entry testEntry;

    @Container
    public static PostgreSQLContainer<?> dictionaryContainer = new PostgreSQLContainer<>("dictionary-postgres:latest");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntryRepository entryRepository;

    @BeforeAll
    static void init() {
        dictionaryContainer.start();
    }

    @AfterAll
    static void shutDown() {
        dictionaryContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        testEntry = new Entry();
        testEntry.setWord("TestEntry");
        testEntry.setWordtype("test.");
        testEntry.setDefinition("Test definition");

        entityManager.persistAndFlush(testEntry);
    }

    @AfterEach
    public void reset() {
        entityManager.remove(testEntry);
        entityManager.flush();
    }

    @Test
    public void whenFindById_thenReturnEntry() {
        Entry found = entryRepository.findById(testEntry.getId()).get();
        assertEquals(testEntry, found);
    }

    @Test
    public void whenFindByWord_thenReturnEntryList() {
        assertTrue(entryRepository.findAll(EntrySpecification.getEntriesByWord(testEntry.getWord())).contains(testEntry));
    }

    @Test
    public void whenFindByType_thenReturnEntryList() {
        assertTrue(entryRepository.findAll(EntrySpecification.getEntriesByType(testEntry.getWordtype())).contains(testEntry));
    }

    @Test
    public void whenFindByLetter_thenReturnEntryList() {
        assertTrue(entryRepository.findAll(EntrySpecification.getEntriesByLetter('T')).contains(testEntry));
    }

    @Test
    public void whenFindAll_thenReturnEntryPage() {
        List<Entry> testEntriesList = Stream.generate(Entry::new)
                .limit(9)
                .peek(entry -> {
                    entry.setWord("TestEntry");
                    entry.setWordtype("test.");
                    entry.setDefinition("Test definition #" + ThreadLocalRandom.current().nextInt());
                })
                .collect(Collectors.toList());

        testEntriesList.forEach(entityManager::persist);
        entityManager.flush();

        testEntriesList.add(0, testEntry);
        Page<Entry> expectedPage = new PageImpl<>(testEntriesList, PageRequest.of(0, 10), 1);
        Page<Entry> returnedPage = entryRepository.findAll(EntrySpecification.getEntriesByWord(testEntry.getWord()), PageRequest.of(0, 10));

        assertEquals(expectedPage, returnedPage);
    }

    @Test
    public void whenDeleteById_thenNotExists() {
        entryRepository.deleteById(testEntry.getId());
        assertFalse(entryRepository.existsById(testEntry.getId()));

        setUp();
        entityManager.persistAndFlush(testEntry);
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + format("jdbc:postgresql://%s:%d/dictionary_db?loggerLevel=OFF", dictionaryContainer.getContainerIpAddress(), dictionaryContainer.getMappedPort(5432)),
                    "spring.datasource.username=" + dictionaryContainer.getUsername(),
                    "spring.datasource.password=" + dictionaryContainer.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

}
