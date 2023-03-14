package shop;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

record ShoppingCartCancelled(UUID shoppingCartId, Instant canceledAt) implements ShoppingCartEvent {

}
