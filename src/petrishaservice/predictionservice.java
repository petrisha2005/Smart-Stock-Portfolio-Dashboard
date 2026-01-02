package petrishaservice;

import java.util.Random;

public class predictionservice {
    public String predictTrend(String stockName) {
        Random rand = new Random();
        int trend = rand.nextInt(3);
        return switch (trend) {
            case 0 -> "📈 The stock might go UP!";
            case 1 -> "📉 The stock might go DOWN!";
            default -> "➖ The stock might stay STABLE!";
        };
    }
}
