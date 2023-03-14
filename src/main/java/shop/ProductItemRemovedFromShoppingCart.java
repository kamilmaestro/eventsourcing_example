package shop;

import java.util.UUID;

record ProductItemRemovedFromShoppingCart(UUID shoppingCartId, PriceProductItem productItem) implements ShoppingCartEvent {

}
