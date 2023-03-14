package shop;

sealed interface ShoppingCartEvent permits
    ShoppingCartOpened,
    ProductItemAddedToShoppingCart,
    ProductItemRemovedFromShoppingCart,
    ShoppingCartConfirmed,
    ShoppingCartCancelled {

}
