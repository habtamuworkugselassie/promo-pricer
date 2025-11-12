package com.promopricer.cart.pricer.service.promotion;

import com.promopricer.cart.pricer.entity.Product;
import com.promopricer.cart.pricer.entity.Promotion;
import com.promopricer.cart.pricer.entity.PromotionType;
import com.promopricer.cart.pricer.service.model.LineItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PromotionRuleService {
    PromotionType getPromotionType();
    void apply(Promotion promotion, Map<UUID, Product> productMap, List<LineItem> lineItems);
}
