package petrishamodel;

import java.util.ArrayList;
import java.util.List;

public class portfolio {
    private String ownerName;
    private List<Stock> stocks;

    public portfolio(String ownerName) {
        this.ownerName = ownerName;
        this.stocks = new ArrayList<>();
    }

    public void addStock(String name, int quantity, double price) {
        // If same stock exists, aggregate quantity and update price to latest
        for (Stock s : stocks) {
            if (s.getName().equalsIgnoreCase(name)) {
                int newQty = s.getQuantity() + quantity;
                s.setQuantity(newQty);
                s.setPrice(price); // update to current/latest price
                return;
            }
        }
        stocks.add(new Stock(name.toUpperCase(), quantity, price));
    }

    public void removeStock(String name) {
        stocks.removeIf(s -> s.getName().equalsIgnoreCase(name));
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getTotalValue() {
        return stocks.stream().mapToDouble(Stock::getTotalValue).sum();
    }

    public void clearPortfolio() {
        stocks.clear();
    }
}
