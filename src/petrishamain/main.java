package petrishamain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import petrishamodel.portfolio;
import petrishaservice.predictionservice;

public class main {
    // Replace with your actual Finnhub API key (yours was included earlier)
    private static final String API_KEY = "d466t6pr01qj716fl4t0d466t6pr01qj716fl4tg";
    private static final String BASE_URL = "https://finnhub.io/api/v1/quote?symbol=";

    public static void main(String[] args) {
        // Symbols to fetch; you can change or read from config
        List<String> symbols = Arrays.asList("AAPL", "TSLA", "MSFT", "AMZN", "GOOG", "NVDA");

        portfolio sharedPortfolio = new portfolio("Patricia");
        predictionservice predictor = new predictionservice();

        System.out.printf("%-10s %-15s %-15s%n", "SYMBOL", "CURRENT PRICE", "CHANGE (%)");
        System.out.println("====================================================");

        for (String symbol : symbols) {
            try {
                String urlString = BASE_URL + symbol + "&token=" + API_KEY;
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

                // Fields: c (current), pc (previous close)
                double currentPrice = json.has("c") && !json.get("c").isJsonNull() ? json.get("c").getAsDouble() : Double.NaN;
                double previousClose = json.has("pc") && !json.get("pc").isJsonNull() ? json.get("pc").getAsDouble() : Double.NaN;

                String changeStr = "N/A";
                if (!Double.isNaN(currentPrice) && !Double.isNaN(previousClose) && previousClose != 0) {
                    double changePercent = ((currentPrice - previousClose) / previousClose) * 100;
                    changeStr = String.format("%.2f", changePercent);
                    System.out.printf("%-10s %-15.2f %-15s%n", symbol, currentPrice, changeStr);
                } else {
                    System.out.printf("%-10s %-15s %-15s%n", symbol, "N/A", "N/A");
                }

                // Add to portfolio (default quantity 10)
                if (!Double.isNaN(currentPrice)) {
                    sharedPortfolio.addStock(symbol, 10, currentPrice);
                }

                // Print prediction to console (Dashboard will also show predictions)
                System.out.println("Prediction: " + predictor.predictTrend(symbol));

                // be polite to API
                Thread.sleep(800);
            } catch (Exception e) {
                System.out.printf("%-10s %-15s %-15s%n", symbol, "N/A", "Error");
                // don't stop the loop on single symbol error
            }
        }

        System.out.println("====================================================");

        // Provide the populated portfolio to Dashboard and launch JavaFX
        Dashboard.setSharedPortfolio(sharedPortfolio);
        // Launch JavaFX dashboard
        Dashboard.launch(Dashboard.class);
    }
}
