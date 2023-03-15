package shop


import com.eventstore.dbclient.*
import spock.lang.Specification

import java.time.Instant

import static shop.ShoppingCart.ShoppingCartStatus.*

class EventStoreDBSpec extends Specification {

  static UUID clientId
  static UUID shoppingCartId
  static PriceProductItem pairOfShoes = new PriceProductItem(
      UUID.fromString("556ed8a6-29cd-467a-99a9-c6341c545758"), 1, BigDecimal.valueOf(200.0)
  )
  static PriceProductItem shirt = new PriceProductItem(
      UUID.fromString("795ac2e9-755e-4f80-9acf-a49a85d244e5"), 1, BigDecimal.valueOf(80.50)
  )

  ShopEventsService service
  EventStoreDBClient eventStore
  String streamName

  void setup() {
    given: "there is a client $clientId"
      clientId = UUID.randomUUID()
    and: "he uses a shopping cart $shoppingCartId"
      shoppingCartId = UUID.randomUUID()

    EventStoreDBClientSettings settings = EventStoreDBConnectionString.parse("esdb://localhost:2113?tls=false")
    eventStore = EventStoreDBClient.create(settings)
    service = new ShopEventsService(eventStore)
    streamName = "shopping_cart-%s".formatted(shoppingCartId)
  }
//  void cleanup() {
//    eventStore.tombstoneStream("shopping_cart-%s".formatted(shoppingCartId))
//    eventStore.deleteStream("shopping_cart-%s".formatted(shoppingCartId), DeleteStreamOptions.get())
//    eventStore.shutdown()
//  }

  def "should append events" () {
    given: "events occured for a shopping cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemAddedToShoppingCart(shoppingCartId, shirt),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes),
          new ShoppingCartConfirmed(shoppingCartId, Instant.now()),
          new ShoppingCartCancelled(shoppingCartId, Instant.now())
      )
      expect: "events are appended to stream in DB"
        WriteResult result = service.appendEvents(streamName, events)
        result.getNextExpectedRevision() == ExpectedRevision.expectedRevision(events.size() - 1)
  }

  def "should get pending status of an opened card" () {
    given: "client: $clientId has opened a cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId)
      )
      service.appendEvents(streamName, events)
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = service.getShoppingCart(streamName)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == []
      shoppingCart.status == PENDING
  }

  def "should get pending status of card with added products" () {
    given: "client: $clientId has added products to cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes)
      )
      service.appendEvents(streamName, events)
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = service.getShoppingCart(streamName)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == [pairOfShoes]
      shoppingCart.status == PENDING
  }

  def "should get pending status of card with removed products" () {
    given: "client: $clientId has removed products from cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes)
      )
      service.appendEvents(streamName, events)
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = service.getShoppingCart(streamName)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == []
      shoppingCart.status == PENDING
  }

  def "should get confirmed status of a confirmed card" () {
    given: "client: $clientId has confirmed a cart: $shoppingCartId with products"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemAddedToShoppingCart(shoppingCartId, shirt),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes),
          new ShoppingCartConfirmed(shoppingCartId, Instant.now())
      )
      service.appendEvents(streamName, events)
    expect: "shopping cart: $shoppingCartId is $CONFIRMED"
      ShoppingCart shoppingCart = service.getShoppingCart(streamName)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == [shirt]
      shoppingCart.status == CONFIRMED
  }

  def "should get cancelled status of a cancelled card" () {
    given: "client: $clientId has cancelled a cart: $shoppingCartId with products"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemAddedToShoppingCart(shoppingCartId, shirt),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes),
          new ShoppingCartConfirmed(shoppingCartId, Instant.now()),
          new ShoppingCartCancelled(shoppingCartId, Instant.now())
      )
      service.appendEvents(streamName, events)
    expect: "shopping cart: $shoppingCartId is $CANCELLED"
      ShoppingCart shoppingCart = service.getShoppingCart(streamName)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == [shirt]
      shoppingCart.status == CANCELLED
  }

}
