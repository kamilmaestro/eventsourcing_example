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
    try {
      return eventStore.appendToStream(
          streamName,
          events.stream().map(this::serialize).iterator()
      ).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
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

  ShoppingCart getShoppingCart(String streamName) {
    try {
      final List<ShoppingCartEvent> events = eventStore.readStream(streamName, ReadStreamOptions.get())
          .get()
          .getEvents()
          .stream()
          .map(this::deserialize)
          .filter(ShoppingCartEvent.class::isInstance)
          .map(ShoppingCartEvent.class::cast)
          .toList();

      return ShoppingCart.getShoppingCartFrom(events);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Object deserialize(ResolvedEvent resolvedEvent) {
    try {
      var eventClass = Class.forName(resolvedEvent.getOriginalEvent().getEventType());
      return EVENTS_MAPPER.readValue(resolvedEvent.getEvent().getEventData(), eventClass);
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
