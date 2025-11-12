package com.promopricer.cart.pricer.service.domains;

import com.promopricer.cart.pricer.config.PromotionAppConfig;
import com.promopricer.cart.pricer.controller.domains.CartItemDto;
import com.promopricer.cart.pricer.models.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
public class LineItem {
    private final UUID productId;
    private final int requestedQty;
    private final Product product;
    private BigDecimal currentPrice;
    private final List<AppliedPromotion> appliedPromotions;

    public LineItem(CartItemDto item, Product product) {
        this.productId = item.productId();
        this.requestedQty = item.qty();
        this.product = product;
        this.currentPrice = product.getPrice().multiply(BigDecimal.valueOf(requestedQty));
        this.appliedPromotions = new java.util.ArrayList<>();
    }

    public void applyDiscount(BigDecimal discountAmount, String promotionName, String description) {
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newPrice = currentPrice.subtract(discountAmount);

            // Constraint: Price must not go negative
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                discountAmount = currentPrice;
                newPrice = BigDecimal.ZERO;
            }

            this.currentPrice = newPrice.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE);

            appliedPromotions.add(new AppliedPromotion(promotionName, description));
        }
    }

}
