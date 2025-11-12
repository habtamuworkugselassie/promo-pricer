package com.promopricer.cart.pricer.service.promotion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promopricer.cart.pricer.config.PromotionAppConfig;
import com.promopricer.cart.pricer.entity.Product;
import com.promopricer.cart.pricer.entity.ProductCategory;
import com.promopricer.cart.pricer.entity.Promotion;
import com.promopricer.cart.pricer.entity.PromotionType;
import com.promopricer.cart.pricer.service.model.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PercentOffCategoryPromotionRuleServiceImpl implements PromotionRuleService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.PERCENT_OFF_CATEGORY;
    }

    @Override
    public void apply(Promotion promotion, Map<UUID, Product> productMap, List<LineItem> lineItems) {

        try {
            @SuppressWarnings("unchecked")
            Map<String, ?> config = mapper.readValue(promotion.getConfig(), Map.class);
            ProductCategory targetCategory = ProductCategory.valueOf((String) config.get("category"));
            int percentage = (Integer) config.get("percentage");

            for (LineItem item : lineItems) {
                if (item.getProduct().getCategory().equals(targetCategory)) {
                    // Calculate discount based on current price (allowing composition)
                    BigDecimal discountRate = BigDecimal.valueOf(percentage).movePointLeft(2);
                    BigDecimal discountAmount = item.getCurrentPrice()
                            .multiply(discountRate, PromotionAppConfig.MONEY_MATH_CONTEXT)
                            .setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE);

                    String description = String.format("%d%% off %s category", percentage, targetCategory);
                    item.applyDiscount(discountAmount, promotion.getName(), description);
                }
            }
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing promotion config: " + e.getMessage());
        }
    }
}
