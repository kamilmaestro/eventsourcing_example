package shop;

import java.time.Instant;
import java.util.UUID;

record ShoppingCartConfirmed(UUID shoppingCartId, Instant confirmedAt) implements ShoppingCartEvent {
}
