package shop;

import java.math.BigDecimal;
import java.util.UUID;

record PriceProductItem(UUID productId, int quantity, BigDecimal price) {

//  double totalAmount() {
//    return quantity * unitPrice;
//  }

}
