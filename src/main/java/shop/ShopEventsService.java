package shop;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.WriteResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class ShopEventsService {

  private static final ObjectMapper EVENTS_MAPPER =
      new JsonMapper()
          .registerModule(new JavaTimeModule())
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  private final EventStoreDBClient eventStore;

  ShopEventsService(EventStoreDBClient eventStore) {
    this.eventStore = eventStore;
  }

  CompletableFuture<WriteResult> appendEvents(String streamName, List<ShoppingCartEvent> events) {
    return eventStore.appendToStream(
        streamName,
        events.stream().map(this::serialize).iterator()
    );
  }

  private EventData serialize(Object event) {
    try {
      return EventDataBuilder
          .json(
              UUID.randomUUID(),
              event.getClass().getTypeName(),
              EVENTS_MAPPER.writeValueAsBytes(event)
          )
          .build();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
