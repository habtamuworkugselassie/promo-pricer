package com.promopricer.cart.pricer.service;

import com.promopricer.cart.pricer.models.CustomerSegment;
import com.promopricer.cart.pricer.models.Product;
import com.promopricer.cart.pricer.models.Promotion;
import com.promopricer.cart.pricer.repository.PromotionRepository;
import com.promopricer.cart.pricer.service.domains.LineItem;
import com.promopricer.cart.pricer.service.promotion.PromotionRuleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PromotionEngineService {

    private final PromotionRepository promotionRepository;
    private final List<PromotionRuleService> orderedRules; // Used for the Chain/Pipeline

    public PromotionEngineService(
            PromotionRepository promotionRepository,
            List<PromotionRuleService> promotionRules
    ) {
        this.promotionRepository = promotionRepository;

        this.orderedRules = promotionRules;
    }

    /**
     * Executes the Chain of Responsibility/Pipeline to calculate final item prices.
     *
     * @param productMap Map of products in the cart for rule context
     * @param lineItems  List of line items to be priced
     * @param customerSegment
     */
    public void applyPromotions(Map<UUID, Product> productMap, List<LineItem> lineItems, CustomerSegment customerSegment) {

        List<Promotion> allPromotions = promotionRepository.findAll();

        List<Promotion> filteredPromotions = allPromotions.stream()
                .filter(p -> promotionAppliesToSegment(p, customerSegment))
                .toList();

        for (PromotionRuleService ruleStrategy : orderedRules) {

            List<Promotion> promotionsForThisRule = filteredPromotions.stream()
                    .filter(p -> p.getType().name().equals(ruleStrategy.getPromotionType().name()))
                    .toList();


            for (Promotion promotion : promotionsForThisRule) {

                ruleStrategy.apply(promotion, productMap, lineItems);
            }
        }
    }


    private boolean promotionAppliesToSegment(Promotion promotion, CustomerSegment customerSegment) {

        String segments = promotion.getTargetSegments();

        if (segments == null || segments.isBlank()) {
            return true;
        }
        return java.util.Arrays.stream(segments.toUpperCase().split(","))
                .map(String::trim)
                .anyMatch(segment -> segment.equals(customerSegment.name()));
    }
}
