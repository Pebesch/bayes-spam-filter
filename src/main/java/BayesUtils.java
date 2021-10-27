import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

public class BayesUtils {
    /**
     * Count the occurrences of words in the map
     * @param map the map to count occurrences
     * @return BigDecimal of count (all values for each key summed up)
     */
    public static Double countOccurrencesInMap(Map<String, Double> map) {
        return map.values().stream().reduce(0d, (a, b) -> a + b);
    }

    /**
     * Gets the probability of a word in a map
     * @param word the word to search for
     * @param map the map to search in
     * @return factor 1 (idempotent) if not present or 1 / all values in the map
     */
    public static Double getProbabilityOfWordInMap(String word, Map<String, Double> map) {
        // https://stackoverflow.com/questions/39851350/reducing-map-by-using-java-8-stream-api
        if(map.containsKey(word)) {
            return 1 / countOccurrencesInMap(map);
        }
        return 1.0;
    }


    /**
     * Calculates the spam probability using Bayes theorem
     * @param spamProb
     * @param hamProb
     * @return P(S|W_1,W_2,…)=P(W_1|S)*P(W_2|S)*…/( P(W_1|S)*P(W_2|S)…+ P(W_1|H)*P(W_2|H)…)
     */
    public static BigDecimal getSpamProbability(BigDecimal spamProb, BigDecimal hamProb) {
        return spamProb.divide(spamProb.add(hamProb), MathContext.DECIMAL128);
    }
}
