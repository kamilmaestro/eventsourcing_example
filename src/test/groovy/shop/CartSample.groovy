package shop

trait CartSample {

  UUID clientId2 = UUID.fromString("6dff4cee-5a3c-4dc7-9305-d18003eea0fa")
  UUID shoppingCartId2 = UUID.fromString("814d8653-57a7-42a9-9828-8464589d0dfd")
  PriceProductItem pairOfShoes2 = new PriceProductItem(
      UUID.fromString("556ed8a6-29cd-467a-99a9-c6341c545758"), 1, BigDecimal.valueOf(200.0)
  )
  ShoppingCartOpened opened = new ShoppingCartOpened(shoppingCartId2, clientId2)

}