package com.promopricer.cart.pricer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "promotion.rules")
public class PromotionRulesProperties {

    private List<String> order;

}
