package org.acme.quarkus.sample;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * A bean consuming data from the "fruit-in" Kafka topic and applying some price
 * conversion. The result is pushed to the "fruit-out" stream.
 */
@ApplicationScoped
public class FruitProcessor {

    private static final double CONVERSION_RATE = 0.88;

    @Incoming("fruit-in")
    @Outgoing("fruit-out")
    @Broadcast
    public String process(Fruit fruit) {
        fruit.price = fruit.price * CONVERSION_RATE;
        Jsonb jsonb = JsonbBuilder.create();
        String result = jsonb.toJson(fruit);
        return result;
    }
}