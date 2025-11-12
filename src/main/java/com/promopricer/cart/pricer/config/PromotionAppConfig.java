package com.promopricer.cart.pricer.config;


import com.promopricer.cart.pricer.service.promotion.PromotionRuleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@AllArgsConstructor
public class PromotionAppConfig {

    private final ApplicationContext applicationContext;

    @Value("${promotion.rules.order}")
    private List<String> promotionRuleOrder;

    @Bean
    public List<PromotionRuleService> promotionRules() {

        Map<String, PromotionRuleService> allPromotionServices =
                applicationContext.getBeansOfType(PromotionRuleService.class);

        List<PromotionRuleService> orderedRules = new ArrayList<>();

        for (String promotionType : promotionRuleOrder) {
            PromotionRuleService service = allPromotionServices.values().stream()
                    .filter(promotionRuleService -> promotionType.equalsIgnoreCase(promotionRuleService.getPromotionType().name()))
                    .findFirst().orElse(null);
            if (service == null) {
                throw new IllegalArgumentException(
                        "Unknown promotion rule bean name: " + promotionType +
                                ". Available beans: " + allPromotionServices.keySet());
            }
            orderedRules.add(service);
        }

        return orderedRules;
    }

    public static final MathContext MONEY_MATH_CONTEXT = new MathContext(2, RoundingMode.HALF_UP);
    public static final RoundingMode MONEY_ROUNDING_MODE = RoundingMode.HALF_UP;
}
