package com.polytech.dictionaryapi;

import com.polytech.dictionaryapi.model.Entry;
import com.polytech.dictionaryapi.util.RestResponsePage;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class DictionaryApiIntegrationTests {

    private static final String DICTIONARY_DB_HOSTNAME = "dictionary_db";
    private static final Entry TEST_ENTRY = new Entry();    //  A new entry for testing POST method
    private static final Entry EXISTING_TEST_ENTRY = new Entry();   //  An entry that already exists in DB when container starts
    private static final int HOST_RANDOM_PORT = ThreadLocalRandom.current().nextInt(10000, 60000);
    private static final Network NETWORK = Network.newNetwork();
    private static final TestRestTemplate TEST_REST_TEMPLATE = new TestRestTemplate();

    @Container
    public static PostgreSQLContainer<?> dictionaryDBContainer = new PostgreSQLContainer<>("dictionary-postgres")
            .withNetwork(NETWORK)
            .withNetworkAliases(DICTIONARY_DB_HOSTNAME);

    @Container
    public static GenericContainer<?> dictionaryApiContainer = new FixedHostPortGenericContainer<>("dictionary-api")
            .withFixedExposedPort(HOST_RANDOM_PORT, 8080)
            .withNetwork(NETWORK)
            .withExposedPorts(8080)
            .withEnv("spring_datasource_url", "jdbc:postgresql://" + DICTIONARY_DB_HOSTNAME + ":5432/dictionary_db?loggerLevel=OFF")
            .withEnv("spring_datasource_username", dictionaryDBContainer.getUsername())
            .withEnv("spring_datasource_password", dictionaryDBContainer.getPassword())
            .withLogConsumer(outputFrame -> {
                if (outputFrame != null && outputFrame.getBytes() != null) {
                    System.out.println(new String(outputFrame.getBytes(), StandardCharsets.UTF_8));
                }
            });

    @BeforeAll
    static void init() {
        dictionaryDBContainer.start();
        dictionaryApiContainer.start();

        HttpClient httpClient = HttpClientBuilder.create().build();
        TEST_REST_TEMPLATE.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        EXISTING_TEST_ENTRY.setId(12L);
        EXISTING_TEST_ENTRY.setWord("Aam");
        EXISTING_TEST_ENTRY.setWordtype("n.");
        EXISTING_TEST_ENTRY.setDefinition("A Dutch and German measure of liquids, varying in different cities, being at Amsterdam about 41 wine gallons, at Antwerp 36 1/2, at Hamburg 38 1/4.");
    }

    @AfterAll
    static void shunDown() {
        dictionaryApiContainer.stop();
        dictionaryDBContainer.stop();
    }

    @Test
    public void givenValidEntry_whenRequestedAddEntry_thenReturnEntry() throws URISyntaxException {
        TEST_ENTRY.setWord("TestWord");
        TEST_ENTRY.setWordtype("test.");
        TEST_ENTRY.setDefinition("Test definition");

        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries";
        ResponseEntity<Entry> actualResponse = TEST_REST_TEMPLATE.postForEntity(url, TEST_ENTRY, Entry.class);

        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertEquals(TEST_ENTRY.getWord(), actualResponse.getBody().getWord());
        assertEquals(TEST_ENTRY.getWordtype(), actualResponse.getBody().getWordtype());
        assertEquals(TEST_ENTRY.getDefinition(), actualResponse.getBody().getDefinition());
    }

    @Test
    public void whenRequestedById_thenReturnEntry() {
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();
        ResponseEntity<Entry> actualResponse = TEST_REST_TEMPLATE.getForEntity(url, Entry.class);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertEquals(EXISTING_TEST_ENTRY, actualResponse.getBody());
    }

    @Test
    public void whenRequestedByWord_thenReturnEntry() {
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/word?word=" + EXISTING_TEST_ENTRY.getWord();
        List<Entry> expectedContent = Collections.singletonList(EXISTING_TEST_ENTRY);
        ParameterizedTypeReference<RestResponsePage<Entry>> typeReference = new ParameterizedTypeReference<RestResponsePage<Entry>>() {};
        ResponseEntity<RestResponsePage<Entry>> actualResponse = TEST_REST_TEMPLATE.exchange(url, HttpMethod.GET, null, typeReference);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertEquals(expectedContent, actualResponse.getBody().getContent());
    }

    @Test
    public void whenRequestedByInvalidWord_thenResponseContainsValidWord() {
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/word?word=Spirng";
        String expectedWord = "Spring";
        ParameterizedTypeReference<RestResponsePage<String>> typeReference = new ParameterizedTypeReference<RestResponsePage<String>>() {};
        ResponseEntity<RestResponsePage<String>> actualResponse = TEST_REST_TEMPLATE.exchange(url, HttpMethod.GET, null, typeReference);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().getContent().contains(expectedWord));
    }

    @Test
    public void givenNewDefinition_whenRequestedDefinitionChange_thenChangeDefinition() {
        String newDefinition = "Updated test definition";
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId() + "?newDefinition=Updated+test+definition";

        TEST_REST_TEMPLATE.exchange(url, HttpMethod.PATCH, null, Void.class);
        url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();
        ResponseEntity<Entry> actualResponse = TEST_REST_TEMPLATE.getForEntity(url, Entry.class);

        assertNotNull(actualResponse.getBody());
        assertEquals(newDefinition, actualResponse.getBody().getDefinition());
    }

    @Test
    public void givenNewEntry_whenRequestedEntryUpdate_thenUpdateEntry() {
        EXISTING_TEST_ENTRY.setWord("UpdatedWord");
        EXISTING_TEST_ENTRY.setWordtype("updatedwordtype.");
        EXISTING_TEST_ENTRY.setDefinition("Updated definition");
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();

        TEST_REST_TEMPLATE.put(url, EXISTING_TEST_ENTRY);
        url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();
        ResponseEntity<Entry> actualResponse = TEST_REST_TEMPLATE.getForEntity(url, Entry.class);

        assertNotNull(actualResponse.getBody());
        assertEquals(EXISTING_TEST_ENTRY, actualResponse.getBody());
    }

    @Test
    public void whenDatabaseDisconnected_thenNotAvailable() {
        whenRequestedById_thenReturnEntry();
        dictionaryDBContainer.getDockerClient().stopContainerCmd(dictionaryDBContainer.getContainerId()).exec();

        assertThrows(AssertionFailedError.class, this::whenRequestedById_thenReturnEntry);

        dictionaryDBContainer.getDockerClient().startContainerCmd(dictionaryDBContainer.getContainerId()).exec();
        whenRequestedById_thenReturnEntry();
    }

    @Test
    public void whenRequestedEntryDelete_thenDeleteEntry() {
        String url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();

        TEST_REST_TEMPLATE.delete(url);
        url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries/" + EXISTING_TEST_ENTRY.getId();
        ResponseEntity<Entry> actualResponse = TEST_REST_TEMPLATE.getForEntity(url, Entry.class);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());

        url = "http://localhost:" + dictionaryApiContainer.getMappedPort(8080) + "/entries";
        TEST_REST_TEMPLATE.postForEntity(url, EXISTING_TEST_ENTRY, Entry.class);
    }

}
