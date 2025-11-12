package com.promopricer.cart.pricer.service;

import com.promopricer.cart.pricer.config.PromotionAppConfig;
import com.promopricer.cart.pricer.controller.domains.*;
import com.promopricer.cart.pricer.models.CustomerSegment;
import com.promopricer.cart.pricer.models.IdempotencyKeyRecord;
import com.promopricer.cart.pricer.models.Product;
import com.promopricer.cart.pricer.exceptions.BadRequestException;
import com.promopricer.cart.pricer.exceptions.ConflictException;
import com.promopricer.cart.pricer.exceptions.ResourceNotFoundException;
import com.promopricer.cart.pricer.repository.IdempotencyKeyRecordRepository;
import com.promopricer.cart.pricer.repository.ProductRepository;
import com.promopricer.cart.pricer.service.domains.LineItem;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final ProductRepository productRepository;
    private final IdempotencyKeyRecordRepository idempotencyRepository;
    private final PromotionEngineService promotionEngine;

    public CartService(ProductRepository productRepository,
                       IdempotencyKeyRecordRepository idempotencyRepository,
                       PromotionEngineService promotionEngine) {
        this.productRepository = productRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.promotionEngine = promotionEngine;
    }

    /**
     * Calculates the price breakdown for a cart request.
     */
    public CartQuoteResponse quoteCart(CartQuoteRequest request) {

        List<UUID> productIds = request.items().stream().map(CartItemDto::productId).toList();

        Map<UUID, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (CartItemDto item : request.items()) {
            if (!productMap.containsKey(item.productId())) {
                throw new ResourceNotFoundException("Product not found with ID: " + item.productId());
            }
            if (productMap.get(item.productId()).getStock() < item.qty()) {

                throw new ConflictException("Product " + item.productId() + " has insufficient stock for quote.");
            }
        }

        List<LineItem> lineItems = request.items().stream()
                .map(item -> new LineItem(item, productMap.get(item.productId())))
                .toList();

        CustomerSegment customerSegment = parseCustomerSegment(request.customerSegment());

        promotionEngine.applyPromotions(productMap, lineItems, customerSegment);

        BigDecimal totalOriginalPrice = BigDecimal.ZERO;
        BigDecimal totalFinalPrice = BigDecimal.ZERO;

        List<PriceBreakdownDto> breakdown = lineItems.stream()
                .map(item -> {
                    BigDecimal originalPrice = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getRequestedQty()));
                    BigDecimal finalPrice = item.getCurrentPrice();
                    BigDecimal discountAmount = originalPrice.subtract(finalPrice);

                    totalOriginalPrice.add(originalPrice); // Note: Immutable BigDecimal
                    totalFinalPrice.add(finalPrice);

                    return new PriceBreakdownDto(
                            item.getProduct().getName() + " (x" + item.getRequestedQty() + ")",
                            originalPrice.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE),
                            finalPrice.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE),
                            discountAmount.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE),
                            item.getAppliedPromotions().stream()
                                    .map(ap -> new AppliedPromotionDto("Promotion", ap.name(), ap.description()))
                                    .toList()
                    );
                })
                .toList();

        BigDecimal totalDiscount = totalOriginalPrice.subtract(totalFinalPrice).setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE);

        return new CartQuoteResponse(
                breakdown,
                totalOriginalPrice.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE),
                totalFinalPrice.setScale(2, PromotionAppConfig.MONEY_ROUNDING_MODE),
                totalDiscount
        );
    }

    /**
     * Validates stock, reserves inventory, and handles idempotency atomically.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CartConfirmResponse confirmCart(CartQuoteRequest request, String idempotencyKey) {

        UUID orderId = UUID.randomUUID();

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return idempotencyRepository.findByKeyId(idempotencyKey)
                    .map(keyRecord -> new CartConfirmResponse(
                            keyRecord.getOrderId(),
                            quoteCart(request).totalFinalPrice(),
                            "Order already confirmed with this Idempotency-Key."
                    ))
                    .orElseGet(() -> processConfirmation(request, orderId, idempotencyKey));
        }

        return processConfirmation(request, orderId, null);
    }

    private CartConfirmResponse processConfirmation(CartQuoteRequest request, UUID orderId, String idempotencyKey) {

        CartQuoteResponse quote = quoteCart(request);
        BigDecimal finalPrice = quote.totalFinalPrice();

        for (CartItemDto item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.productId()));

            int quantityToReserve = item.qty();

            if (product.getStock() < quantityToReserve) {
                throw new ConflictException("Insufficient stock for product: " + product.getName() + ". Available: " + product.getStock());
            }

            product.setStock(product.getStock() - quantityToReserve);

            try {
                productRepository.save(product);
            } catch (OptimisticLockingFailureException e) {
                throw new ConflictException("Stock reservation failed due to concurrent update for product: " + product.getName());
            }
        }

        if (idempotencyKey != null) {
            idempotencyRepository.save(
                    IdempotencyKeyRecord.builder()
                            .keyId(idempotencyKey).orderId(orderId)
                            .build());
        }

        return new CartConfirmResponse(orderId, finalPrice, "Order confirmed successfully.");
    }

    private CustomerSegment parseCustomerSegment(String segment) {
        try {
            return CustomerSegment.valueOf(segment.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid customer segment: '" + segment + "'. Must be one of: " +
                    java.util.Arrays.toString(CustomerSegment.values()));
        }
    }
}
