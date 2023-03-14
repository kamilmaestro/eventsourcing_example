package shop;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
class ShoppingCart {

  enum ShoppingCartStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
  }

  UUID id;
  UUID clientId;
  ShoppingCartStatus status;
  final Collection<PriceProductItem> productItems = new ArrayList<>();
  Instant confirmedAt;
  Instant canceledAt;

  static ShoppingCart getShoppingCartFrom(List<ShoppingCartEvent> events) {
    ShoppingCart shoppingCart = new ShoppingCart();
    for (ShoppingCartEvent event: events) {
      shoppingCart.apply(event);
    }

    return shoppingCart;
  }

  private void apply(ShoppingCartEvent event) {
    switch (event) {
      case ShoppingCartOpened opened -> apply(opened);
      case ProductItemAddedToShoppingCart added -> apply(added);
      case ProductItemRemovedFromShoppingCart removed -> apply(removed);
      case ShoppingCartConfirmed confirmed -> apply(confirmed);
      case ShoppingCartCancelled cancelled -> apply(cancelled);
    }
  }

  private void apply(ShoppingCartOpened event) {
    this.id = event.shoppingCartId();
    this.clientId = event.clientId();
    this.status = ShoppingCartStatus.PENDING;
  }

  private void apply(ProductItemAddedToShoppingCart event) {
    this.productItems.add(event.productItem());
  }

  private void apply(ProductItemRemovedFromShoppingCart event) {
    this.productItems
        .removeIf(product -> product.productId().equals(event.productItem().productId()));
  }

  private void apply(ShoppingCartConfirmed event) {
    this.status = ShoppingCartStatus.CONFIRMED;
  }

  private void apply(ShoppingCartCancelled event) {
    this.productItems.clear();
    this.status = ShoppingCartStatus.CANCELLED;
  }

}
