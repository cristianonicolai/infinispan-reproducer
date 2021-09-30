package org.acme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;

import org.infinispan.client.hotrod.DataFormat;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.commons.dataconversion.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class GreetingResourceTest {

    static final String PROTOBUF_METADATA_CACHE_NAME = "___protobuf_metadata";
    static InfinispanContainer INFINISPAN = new InfinispanContainer();
    static String CACHE_NAME = "travels";

    @Inject
    JsonDataFormatMarshaller marshaller;

    @Inject
    ObjectMapper mapper;

    @Inject
    RemoteCacheManager manager;

    DataFormat jsonDataFormat;

    @BeforeAll
    public static void start() {
        INFINISPAN.start();
    }

    @AfterAll
    public static void stop() {
        INFINISPAN.stop();
    }

    @BeforeEach
    public void init() {
        jsonDataFormat = DataFormat.builder().valueType(MediaType.APPLICATION_JSON).valueMarshaller(marshaller).build();
        manager.start();

        String xml = "<local-cache name=\"travels\">" +
                "<indexing storage=\"filesystem\" path=\"travels\">" +
                "<indexed-entities>" +
                "<indexed-entity>org.acme.travels.travels.Travels</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.Flight</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.VisaApplication</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.Address</indexed-entity>" +
                "<indexed-entity>org.kie.kogito.index.model.ProcessInstanceMeta</indexed-entity>" +
                "<indexed-entity>org.kie.kogito.index.model.UserTaskInstanceMeta</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.Traveller</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.Trip</indexed-entity>" +
                "<indexed-entity>org.acme.travels.travels.Hotel</indexed-entity>" +
                "<indexed-entity>org.kie.kogito.index.model.KogitoMetadata</indexed-entity>" +
                "</indexed-entities>" +
                "</indexing>" +
                "<persistence>" +
                "<file-store/>" +
                "</persistence>" +
                "</local-cache>";
        manager.administration().createCache(CACHE_NAME, new XMLStringConfiguration(xml));
    }

    @Test
    public void test() {
        try {
            logProtoCacheKeys();

//            String travelsProtoFile = "travels.proto";
//            Path proto = Paths.get(Thread.currentThread().getContextClassLoader().getResource(travelsProtoFile).toURI());
//            manager.getCache(PROTOBUF_METADATA_CACHE_NAME).put(travelsProtoFile, Files.readString(proto));

            String id = UUID.randomUUID().toString();
            ObjectNode json = mapper.createObjectNode();
            json.put("_type", "org.acme.travels.travels.Travels");
            json.put("id", id);

            manager.getCache(CACHE_NAME).withDataFormat(jsonDataFormat).put(id, json);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage(), ex);
        }
    }

    private void logProtoCacheKeys() {
        System.out.println(">>>>>>list cache keys start");
        manager.getCache(PROTOBUF_METADATA_CACHE_NAME).entrySet().forEach(System.out::println);
        System.out.println(">>>>>>list cache keys end");
    }
}