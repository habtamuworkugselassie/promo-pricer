package com.promopricer.cart.pricer.controller;


import com.promopricer.cart.pricer.controller.domains.PromotionRequest;
import com.promopricer.cart.pricer.models.Promotion;
import com.promopricer.cart.pricer.models.PromotionType;
import com.promopricer.cart.pricer.repository.PromotionRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/promotions")
class PromotionController {
    private final PromotionRepository promotionRepository;

    @PostMapping
    public ResponseEntity<Promotion> createPromotion(@Valid @RequestBody PromotionRequest request) {

        Promotion promotion = new Promotion();
        promotion.setName(request.name());
        promotion.setType(PromotionType.valueOf(request.type()));
        promotion.setConfig(request.config());
        promotion.setTargetSegments(request.targetSegments());
        Promotion saved = promotionRepository.save(promotion);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
