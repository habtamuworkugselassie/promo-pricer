package com.promopricer.cart.pricer.service.promotion;

import com.promopricer.cart.pricer.models.Product;
import com.promopricer.cart.pricer.models.Promotion;
import com.promopricer.cart.pricer.models.PromotionType;
import com.promopricer.cart.pricer.service.domains.LineItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PromotionRuleService {
    PromotionType getPromotionType();
    void apply(Promotion promotion, Map<UUID, Product> productMap, List<LineItem> lineItems);
}
