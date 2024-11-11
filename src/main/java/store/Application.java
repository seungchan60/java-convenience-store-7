package store;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import camp.nextstep.edu.missionutils.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) {
        boolean continueShopping = true;
        while (continueShopping) {
            OutputView.printWelcomeMessage();
            List<Product> products = OutputView.loadProducts("src/main/resources/products.md");
            OutputView.printProductList(products);
            boolean validInput = false;
            String userInput = "";

            while (!validInput) {
                try {
                    userInput = InputView.readItemInput();
                    InputView.validateItemInput(userInput);
                    validInput = true;
                } catch (IllegalArgumentException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
            }

            System.out.println("입력하신 내용: " + userInput);
            Map<String, Integer> purchasedItems = InputView.parsePurchasedItems(userInput);
            Map<String, Integer> promotionItems = PromotionChecker.checkPromotions(purchasedItems, "src/main/resources/promotions.md", products);
            int finalPrice = Membership.applyMembershipDiscount(purchasedItems, products);
            OutputView.printReceipt(purchasedItems, promotionItems, finalPrice, products);

            System.out.println("감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)");
            String response = Console.readLine();
            if (!response.equalsIgnoreCase("Y")) {
                continueShopping = false;
            }
        }
    }
}

class Product {
    String name;
    int price;
    int quantity;
    String promotion;

    public Product(String name, int price, int quantity, String promotion) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.promotion = promotion;
    }
}

class OutputView {
    public static void printWelcomeMessage() {
        System.out.println("안녕하세요. W편의점입니다.");
        System.out.println("현재 보유하고 있는 상품입니다.");
        System.out.println();
    }

    public static List<Product> loadProducts(String filePath) {
        List<Product> products = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("name")) { // Skip header line
                    String[] productData = line.split(",");
                    String name = productData[0];
                    int price = Integer.parseInt(productData[1]);
                    int quantity = Integer.parseInt(productData[2]);
                    String promotion = productData[3].equals("null") ? "" : productData[3];
                    products.add(new Product(name, price, quantity, promotion));
                }
            }
        } catch (IOException e) {
            System.err.println("파일을 읽는 도중 오류가 발생했습니다: " + e.getMessage());
        }
        return products;
    }

    public static void printProductList(List<Product> products) {
        Map<String, Boolean> printedItems = new HashMap<>();
        for (Product product : products) {
            System.out.println(String.format("- %s %,d원 %d개 %s", product.name, product.price, product.quantity, product.promotion));
            printedItems.put(product.name, true);
            if (!product.promotion.isEmpty() && products.stream().noneMatch(p -> p.name.equals(product.name) && p.promotion.isEmpty())) {
                System.out.println(String.format("- %s %,d원 재고 없음", product.name, product.price));
            }
            printedItems.put(product.name, true);
            if (!product.promotion.isEmpty() && product.quantity == 0) {
                System.out.println(String.format("- %s %,d원 재고 없음", product.name, product.price));
            }
        }
    }

    public static void printReceipt(Map<String, Integer> purchasedItems, Map<String, Integer> promotionItems, int finalPrice, List<Product> products) {
        System.out.println("\n================W 편의점================");
        System.out.println("상품명\t\t수량\t금액");
        int totalPrice = 0;
        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            int price = quantity * getProductPrice(itemName, products);
            totalPrice += price;
            System.out.printf("%s\t\t%d\t%,d\n", itemName, quantity, price);
        }
        System.out.println("===============증정===============");
        for (Map.Entry<String, Integer> entry : promotionItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            System.out.printf("%s\t\t%d\n", itemName, quantity);
        }
        System.out.println("===================================");
        int discount = PromotionChecker.calculatePromotionDiscount(promotionItems, products);
        System.out.printf("총구매액\t%d\t%,d\n", purchasedItems.values().stream().mapToInt(Integer::intValue).sum(), totalPrice);
        System.out.printf("행사할인\t\t-%,d\n", discount);
        int membershipDiscount = Math.min((int) (totalPrice * 0.3), 8000);
        System.out.printf("멤버십할인\t\t-%,d\n", membershipDiscount);
        System.out.printf("내실돈\t\t%,d\n", finalPrice - discount - membershipDiscount);
    }

    private static int getProductPrice(String itemName, List<Product> products) {
        for (Product product : products) {
            if (product.name.equals(itemName)) {
                return product.price;
            }
        }
        return 0;
    }
}

class InputView {
    public static String readItemInput() {
        System.out.println("구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])");
        return Console.readLine();
    }

    public static void validateItemInput(String input) {
        String regex = "^\\[(.+-\\d+)\\](,\\[(.+-\\d+)\\])*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("입력 형식이 올바르지 않습니다. [목록이름-수량] 형식을 지켜주세요.");
        }
    }

    public static Map<String, Integer> parsePurchasedItems(String input) {
        Map<String, Integer> items = new HashMap<>();
        String[] itemEntries = input.split(",");
        for (String entry : itemEntries) {
            entry = entry.replaceAll("[\\[\\]]", "");
            String[] parts = entry.split("-");
            String itemName = parts[0];
            int quantity = Integer.parseInt(parts[1]);
            items.put(itemName, quantity);
        }
        return items;
    }
}

class PromotionChecker {
    public static Map<String, Integer> checkPromotions(Map<String, Integer> purchasedItems, String promotionsFilePath, List<Product> products) {
        Map<String, Integer> promotionItems = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(promotionsFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("name")) { // Skip header line
                    String[] promotionData = line.split(",");
                    String promotionName = promotionData[0];
                    int buyQuantity = Integer.parseInt(promotionData[1]);
                    int getQuantity = Integer.parseInt(promotionData[2]);

                    for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
                        String itemName = entry.getKey();
                        int quantity = entry.getValue();

                        if (promotionName.equals("탄산2+1") && itemName.matches(".*콜라|사이다|탄산수.*") && quantity >= buyQuantity) {
                            promotionItems.put(itemName, promotionItems.getOrDefault(itemName, 0) + getQuantity);
                            System.out.println(itemName + " " + getQuantity + "개가 추가되었습니다.");
                        } else if (itemName.equals(promotionName) && quantity >= buyQuantity) {
                            promotionItems.put(itemName, promotionItems.getOrDefault(itemName, 0) + getQuantity);
                            System.out.println(itemName + " " + getQuantity + "개가 추가되었습니다.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("프로모션 파일을 읽는 도중 오류가 발생했습니다: " + e.getMessage());
        }
        return promotionItems;
    }

    public static int calculatePromotionDiscount(Map<String, Integer> promotionItems, List<Product> products) {
        int discount = 0;
        for (Map.Entry<String, Integer> entry : promotionItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            for (Product product : products) {
                if (product.name.equals(itemName)) {
                    discount += product.price * quantity;
                    break; // Ensure we calculate discount only once for each product
                }
            }
        }
        return discount;
    }
}

class Membership {
    public static int applyMembershipDiscount(Map<String, Integer> purchasedItems, List<Product> products) {
        System.out.println("멤버십 할인을 받으시겠습니까? (Y/N)");
        String response = Console.readLine();
        int totalPrice = 0;
        int nonPromoPrice = 0;

        // Calculate total price and non-promo price
        for (Map.Entry<String, Integer> entry : purchasedItems.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();
            for (Product product : products) {
                if (product.name.equals(itemName)) {
                    totalPrice += product.price * quantity;
                    if (product.promotion == null || product.promotion.isEmpty()) {
                        nonPromoPrice += product.price * quantity;
                    }
                }
            }
        }

        int discount = 0;
        if (response.equalsIgnoreCase("Y")) {
            // Apply membership discount
            discount = (int) (nonPromoPrice * 0.3);
            discount = Math.min(discount, 8000); // Max discount limit is 8000
            totalPrice -= discount;
        } else {
            System.out.println("멤버십 할인을 적용하지 않았습니다.");
        }
        return totalPrice;
    }
}
