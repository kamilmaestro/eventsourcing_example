package shop;

import com.eventstore.dbclient.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

  WriteResult appendEvents(String streamName, List<ShoppingCartEvent> events) {
    throw new UnsupportedOperationException(
        "Use eventStore.appendToStream() to append events and then serialize events with the usage of serialize(event) method !!!"
    );
  }

  private EventData serialize(Object event) {
    throw new UnsupportedOperationException("Create EventData object converting it from event -> EventDataBuilder !!!");
  }

}
