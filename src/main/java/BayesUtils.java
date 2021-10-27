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
    public static BigDecimal countOccurrencesInMap(Map<String, Double> map) {
        return BigDecimal.valueOf(map.values().stream().reduce(0d, (a, b) -> a + b));
    }

    /**
     * Gets the probability of a word in a map
     * @param word the word to search for
     * @param map the map to search in
     * @return factor 1 (idempotent) if not present or 1 / all values in the map
     */
    public static BigDecimal getProbabilityOfWordInMap(String word, Map<String, Double> map) {
        // https://stackoverflow.com/questions/39851350/reducing-map-by-using-java-8-stream-api
        if(map.containsKey(word)) {
            return BigDecimal.ONE.divide(countOccurrencesInMap(map), MathContext.DECIMAL128);
        }
        return BigDecimal.ONE;
    }


    /**
     * Calculates the spam probability using Bayes theorem
     * @param spamProb
     * @param hamProb
     * @param spamThreshold
     * @return
     */
    public static BigDecimal getSpamProbability(BigDecimal spamProb, BigDecimal hamProb, BigDecimal spamThreshold) {
        BigDecimal hamThreshold = BigDecimal.ONE.subtract(spamThreshold);
        BigDecimal counter = spamThreshold.multiply(spamProb);
        BigDecimal denominatorSpam = spamThreshold.multiply(spamProb);
        BigDecimal denominatorHam = hamThreshold.multiply(hamProb);
        BigDecimal denominator = denominatorSpam.add(denominatorHam, MathContext.DECIMAL128);
        return counter.divide(denominator, MathContext.DECIMAL128);
    }
}
