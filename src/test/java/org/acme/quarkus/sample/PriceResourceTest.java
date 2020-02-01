package org.acme.quarkus.sample;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

@QuarkusTest
class PriceResourceTest {

    private static final String PRICES_SSE_ENDPOINT = "http://localhost:8081/fruits/stream";

    private static final KafkaContainer KAFKA = new KafkaContainer();

    @BeforeAll
    public static void configureKafkaLocation() {
        KafkaContainer KAFKA = new KafkaContainer();
        KAFKA.start();
        System.setProperty("kafka.bootstrap.servers", KAFKA.getBootstrapServers());
    }

    @AfterAll
    public static void clearKafkaLocation() {
        System.clearProperty("kafka.bootstrap.servers");
        KAFKA.close();
    }

    @Test
    void testPricesEventStream() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(PRICES_SSE_ENDPOINT);

        List<Fruit> received = new CopyOnWriteArrayList<>();

        SseEventSource source = SseEventSource.target(target).build();
        source.register(inboundSseEvent -> received.add(inboundSseEvent.readData(Fruit.class)));
        source.open();
        await().atMost(100000, MILLISECONDS).until(() -> received.size() == 3);
        source.close();
    }
}