package shop;

import java.util.UUID;

record ShoppingCartOpened(UUID shoppingCartId, UUID clientId) implements ShoppingCartEvent {

}
