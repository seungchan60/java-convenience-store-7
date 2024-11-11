package store.util;

import store.model.Product;
import store.model.Promotion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    public List<Product> loadProducts(String filePath) {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                String name = values[0];
                int price = Integer.parseInt(values[1]);
                int quantity = Integer.parseInt(values[2]);
                String promotion = values[3].equals("null") ? null : values[3];
                products.add(new Product(name, price, quantity, promotion));
            }
        } catch (IOException e) {
            throw new IllegalStateException("[ERROR] 파일을 읽을 수 없습니다: " + filePath, e);
        }
        return products;
    }

    public List<Promotion> loadPromotions(String filePath) {
        List<Promotion> promotions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                String name = values[0];
                int buyQuantity = Integer.parseInt(values[1]);
                int freeQuantity = Integer.parseInt(values[2]);
                LocalDate startDate = LocalDate.parse(values[3]);
                LocalDate endDate = LocalDate.parse(values[4]);
                promotions.add(new Promotion(name, buyQuantity, freeQuantity, startDate, endDate));
            }
        } catch (IOException e) {
            throw new IllegalStateException("[ERROR] 파일을 읽을 수 없습니다: " + filePath, e);
        }
        return promotions;
    }
}
