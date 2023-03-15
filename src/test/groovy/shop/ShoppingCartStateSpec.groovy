package shop

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import spock.lang.Specification
import com.eventstore.dbclient.*
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow
import static shop.ShoppingCart.ShoppingCartStatus.CANCELLED
import static shop.ShoppingCart.ShoppingCartStatus.CONFIRMED
import static shop.ShoppingCart.ShoppingCartStatus.PENDING

class ShoppingCartStateSpec extends Specification implements CartSample {

  static UUID clientId = UUID.fromString("6dff4cee-5a3c-4dc7-9305-d18003eea0fa")
  static UUID shoppingCartId = UUID.fromString("814d8653-57a7-42a9-9828-8464589d0dfd")
  static PriceProductItem pairOfShoes = new PriceProductItem(
      UUID.fromString("556ed8a6-29cd-467a-99a9-c6341c545758"), 1, BigDecimal.valueOf(200.0)
  )
  static PriceProductItem shirt = new PriceProductItem(
      UUID.fromString("795ac2e9-755e-4f80-9acf-a49a85d244e5"), 1, BigDecimal.valueOf(80.50)
  )

  def "should get pending status of opened card" () {
    given: "client: $clientId has opened cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId)
      )
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCartFrom(events)
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
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCartFrom(events)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == [pairOfShoes]
      shoppingCart.status == PENDING
  }

  def "should get pending status of card with removed products" () {
    given: "client: $clientId has removed products to cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes)
      )
    expect: "shopping cart: $shoppingCartId is $PENDING"
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCartFrom(events)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == []
      shoppingCart.status == PENDING
  }

  def "should get confirmed status of a confirmed card" () {
    given: "client: $clientId has confirmed a cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemAddedToShoppingCart(shoppingCartId, shirt),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes),
          new ShoppingCartConfirmed(shoppingCartId, Instant.now())
      )
    expect: "shopping cart: $shoppingCartId is $CONFIRMED"
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCartFrom(events)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == [shirt]
      shoppingCart.status == CONFIRMED
  }

  def "should get cancelled status of a cancelled card" () {
    given: "client: $clientId has cancell a cart: $shoppingCartId"
      List<ShoppingCartEvent> events = List.of(
          new ShoppingCartOpened(shoppingCartId, clientId),
          new ProductItemAddedToShoppingCart(shoppingCartId, pairOfShoes),
          new ProductItemAddedToShoppingCart(shoppingCartId, shirt),
          new ProductItemRemovedFromShoppingCart(shoppingCartId, pairOfShoes),
          new ShoppingCartCancelled(shoppingCartId, Instant.now())
      )
    expect: "shopping cart: $shoppingCartId is $CANCELLED"
      ShoppingCart shoppingCart = ShoppingCart.getShoppingCartFrom(events)
      shoppingCart.id == shoppingCartId
      shoppingCart.clientId == clientId
      shoppingCart.productItems == []
      shoppingCart.status == CANCELLED
  }

}
