package com.promopricer.cart.pricer.service.domains;

import com.promopricer.cart.pricer.models.PromotionType;

public record AppliedPromotion(PromotionType type, String name, String description) {
}
