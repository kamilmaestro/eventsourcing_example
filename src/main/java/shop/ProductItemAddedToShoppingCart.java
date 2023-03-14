package shop;

import java.util.UUID;

record ProductItemAddedToShoppingCart(UUID shoppingCartId, PriceProductItem productItem) implements ShoppingCartEvent {

}
