package shop;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
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
    throw new UnsupportedOperationException();
  }

}
