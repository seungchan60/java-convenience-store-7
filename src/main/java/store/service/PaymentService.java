package store.service;

import store.model.Product;
import store.model.Promotion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaymentService {

    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

    public int calculateTotalAmount(Map<String, Integer> purchaseRequests, List<Product> products, List<Promotion> promotions, boolean applyMembershipDiscount) {
        int totalAmount = 0;
        int promotionDiscount = 0;

        for (Map.Entry<String, Integer> entry : purchaseRequests.entrySet()) {
            String productName = entry.getKey();
            int requestedQuantity = entry.getValue();

            Product product = findProductByName(products, productName);
            if (product == null) continue;

            // 상품 가격 계산
            int productTotal = product.getPrice() * requestedQuantity;

            // 프로모션 적용 여부 확인
            Optional<Promotion> promotion = findApplicablePromotion(product, promotions);
            if (promotion.isPresent()) {
                int freeQuantity = calculatePromotionFreeQuantity(requestedQuantity, promotion.get());
                promotionDiscount += freeQuantity * product.getPrice(); // 프로모션 할인 금액 누적
            }

            totalAmount += productTotal;
        }

        // 총 구매 금액에서 프로모션 할인 적용
        totalAmount -= promotionDiscount;

        // 멤버십 할인 적용
        if (applyMembershipDiscount) {
            int membershipDiscount = (int) Math.min(totalAmount * MEMBERSHIP_DISCOUNT_RATE, MAX_MEMBERSHIP_DISCOUNT);
            totalAmount -= membershipDiscount;
        }

        return totalAmount;
    }

    private Product findProductByName(List<Product> products, String productName) {
        return products.stream()
                .filter(product -> product.getName().equals(productName))
                .findFirst()
                .orElse(null);
    }

    private Optional<Promotion> findApplicablePromotion(Product product, List<Promotion> promotions) {
        return promotions.stream()
                .filter(promotion -> promotion.getName().equals(product.getPromotion()))
                .findFirst();
    }

    private int calculatePromotionFreeQuantity(int quantity, Promotion promotion) {
        int applicableSets = quantity / promotion.getBuyQuantity();
        return applicableSets * promotion.getFreeQuantity();
    }
}
