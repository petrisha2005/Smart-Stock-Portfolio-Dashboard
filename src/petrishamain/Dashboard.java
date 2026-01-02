package petrishamain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import petrishamodel.Stock;
import petrishamodel.portfolio;
import petrishaservice.predictionservice;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Dashboard extends Application {
    private static portfolio sharedPortfolio = null; // set by main
    private portfolio portfolioLocal;
    private TableView<Stock> table;
    private ObservableList<Stock> data;
    private Label totalValueLabel;
    private predictionservice predictor = new predictionservice();

    public static void setSharedPortfolio(portfolio p) {
        sharedPortfolio = p;
    }

    @Override
    public void start(Stage stage) {
        // Use shared portfolio if provided, else create new
        portfolioLocal = (sharedPortfolio != null) ? sharedPortfolio : new portfolio("Patricia");
        data = FXCollections.observableArrayList(portfolioLocal.getStocks());

        // Table setup
        table = new TableView<>();
        TableColumn<Stock, String> nameCol = new TableColumn<>("Stock");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(120);

        TableColumn<Stock, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        qtyCol.setPrefWidth(80);

        TableColumn<Stock, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()).asObject());
        priceCol.setPrefWidth(100);

        TableColumn<Stock, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getTotalValue()).asObject());
        totalCol.setPrefWidth(120);

        table.getColumns().addAll(nameCol, qtyCol, priceCol, totalCol);
        table.setItems(data);

        // Controls
        TextField stockField = new TextField();
        stockField.setPromptText("Stock (eg AAPL)");
        stockField.setPrefWidth(100);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Qty");
        qtyField.setPrefWidth(60);

        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefWidth(80);

        Button addBtn = new Button("Add");
        Button removeBtn = new Button("Remove");
        Button saveBtn = new Button("Save");
        Button loadBtn = new Button("Load");
        Button chartBtn = new Button("Chart");
        Button predictBtn = new Button("Predict");
        Button clearBtn = new Button("Clear All");

        addBtn.setOnAction(e -> {
            try {
                String name = stockField.getText().trim().toUpperCase();
                int qty = Integer.parseInt(qtyField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                portfolioLocal.addStock(name, qty, price);
                refreshData();
                stockField.clear(); qtyField.clear(); priceField.clear();
            } catch (Exception ex) {
                showAlert("Invalid input. Example: AAPL 10 185.50");
            }
        });

        // allow Enter key in priceField to add
        priceField.setOnKeyPressed(k -> {
            if (k.getCode() == KeyCode.ENTER) addBtn.fire();
        });

        removeBtn.setOnAction(e -> {
            Stock sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                portfolioLocal.removeStock(sel.getName());
                refreshData();
            } else showAlert("Select a stock to remove.");
        });

        saveBtn.setOnAction(e -> {
            try (FileWriter writer = new FileWriter("portfolio.json")) {
                new Gson().toJson(portfolioLocal.getStocks(), writer);
                showAlert("Portfolio saved to portfolio.json");
            } catch (IOException ex) {
                showAlert("Error saving portfolio: " + ex.getMessage());
            }
        });

        loadBtn.setOnAction(e -> {
            try (FileReader reader = new FileReader("portfolio.json")) {
                List<Stock> list = new Gson().fromJson(reader, new TypeToken<List<Stock>>(){}.getType());
                portfolioLocal.clearPortfolio();
                if (list != null) list.forEach(s -> portfolioLocal.addStock(s.getName(), s.getQuantity(), s.getPrice()));
                refreshData();
                showAlert("Portfolio loaded.");
            } catch (IOException ex) {
                showAlert("No saved portfolio found.");
            }
        });

        chartBtn.setOnAction(e -> showChart());
        predictBtn.setOnAction(e -> {
            Stock s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                String message = predictor.predictTrend(s.getName());
                showAlert(s.getName() + " -> " + message);
            } else showAlert("Select a stock to predict.");
        });

        clearBtn.setOnAction(e -> {
            portfolioLocal.clearPortfolio();
            refreshData();
        });

        HBox controls = new HBox(8, stockField, qtyField, priceField, addBtn, removeBtn, saveBtn, loadBtn, chartBtn, predictBtn, clearBtn);
        controls.setPadding(new Insets(12));

        totalValueLabel = new Label();
        totalValueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        refreshTotal();

        VBox center = new VBox(8, table, totalValueLabel);
        center.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(center);

        Scene scene = new Scene(root, 980, 520);
        stage.setScene(scene);
        stage.setTitle("📊 Patricia's Stock Portfolio");
        stage.show();
    }

    private void refreshData() {
        data.setAll(portfolioLocal.getStocks());
        refreshTotal();
    }

    private void refreshTotal() {
        totalValueLabel.setText("Total Portfolio Value: ₹" + String.format("%.2f", portfolioLocal.getTotalValue()));
    }

    private void showChart() {
        PieChart pie = new PieChart();
        for (Stock s : portfolioLocal.getStocks()) {
            PieChart.Data d = new PieChart.Data(s.getName(), s.getTotalValue());
            pie.getData().add(d);
        }
        Stage st = new Stage();
        st.setTitle("Portfolio Breakdown");
        VBox box = new VBox(pie);
        box.setPadding(new Insets(10));
        st.setScene(new Scene(box, 480, 360));
        st.show();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
