package store.service;

import store.model.Product;
import store.model.Promotion;

import java.util.List;
import java.util.Optional;

public class ProductService {

    public boolean checkStockAvailability(Product product, int requestedQuantity) {
        return product.getQuantity() >= requestedQuantity;
    }

    public Optional<Promotion> findApplicablePromotion(Product product, List<Promotion> promotions) {
        return promotions.stream()
                .filter(promotion -> promotion.getName().equals(product.getPromotion()))
                .findFirst();
    }

    public int calculatePromotionFreeQuantity(int quantity, Promotion promotion) {
        int applicableSets = quantity / promotion.getBuyQuantity();
        return applicableSets * promotion.getFreeQuantity();
    }
}
