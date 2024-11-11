package store.view;

import store.model.Product;

import java.util.List;

public class OutputView {
    public void printWelcomeMessage() {
        System.out.println("안녕하세요. W편의점입니다.");
        System.out.println("현재 보유하고 있는 상품입니다.");
    }

    public void printProductList(List<Product> products) {
        for (Product product : products) {
            String productInfo = "- " + product.getName() + " " + product.getPrice() + "원 ";

            if (product.getQuantity() > 0) {
                productInfo += product.getQuantity() + "개";
                if (product.getPromotion() != null) {
                    productInfo += " " + product.getPromotion();
                }
            } else {
                productInfo += "재고 없음";
            }
            System.out.println(productInfo);
        }
    }
}
