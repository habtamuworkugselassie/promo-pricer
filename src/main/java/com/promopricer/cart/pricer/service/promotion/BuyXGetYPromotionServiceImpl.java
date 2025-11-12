package com.promopricer.cart.pricer.service.promotion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promopricer.cart.pricer.models.Product;
import com.promopricer.cart.pricer.models.Promotion;
import com.promopricer.cart.pricer.models.PromotionType;
import com.promopricer.cart.pricer.service.domains.LineItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BuyXGetYPromotionServiceImpl implements PromotionRuleService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.BUY_X_GET_Y;
    }

    @Override
    public void apply(Promotion promotion, Map<UUID, Product> productMap, List<LineItem> lineItems) {

        try {
            @SuppressWarnings("unchecked")
            Map<String, ?> config = mapper.readValue(promotion.getConfig(), Map.class);
            UUID targetProductId = UUID.fromString((String) config.get("productId"));
            int buy = (Integer) config.get("buy");
            int get = (Integer) config.get("get");

            for (LineItem item : lineItems) {
                if (item.getProductId().equals(targetProductId)) {

                    int totalQty = item.getRequestedQty();
                    int fullSetSize = buy + get;

                    int freeItems = (totalQty / fullSetSize) * get;

                    if (freeItems > 0) {

                        BigDecimal discountPerItem = item.getProduct().getPrice();
                        BigDecimal discountAmount = discountPerItem
                                .multiply(BigDecimal.valueOf(freeItems));

                        String description = String.format("Buy %d Get %d Free (x%d free items)", buy, get, freeItems);
                        item.applyDiscount(discountAmount, promotion.getName(), description);
                    }
                }
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            System.err.println("Error parsing promotion config for BxGy: " + e.getMessage());
        }
    }
}
