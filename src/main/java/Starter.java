import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Starter {
    // https://www.youtube.com/watch?v=O2L2Uv9pdDA&ab_channel=StatQuestwithJoshStarmer
    public static void main(String[] args) {
        final double ALPHA = 0.001;
        final double S_THRESHOLD = 0.5;
        final int topWordsToCompare = 1000;
        final int DEBUG_ENTRIES = 15;

        printBanner("Start");
        // a) Read all files

        // Read all ham mails
        printBanner("Top Ham Words");
        Map<String, Double> hamWords = new HashMap<>();
        for (File file : returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test")) {
            readWordsAndPutCount(hamWords, file);
        }
        showTopNEntries(hamWords, DEBUG_ENTRIES);
        printBanner("Size of Ham Words");
        System.out.println(hamWords.keySet().size());

        // Read all spam mails
        printBanner("Top Spam Words");
        Map<String, Double> spamWords = new HashMap<>();
        for (File file : returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test")) {
            readWordsAndPutCount(spamWords, file);
        }
        showTopNEntries(spamWords, DEBUG_ENTRIES);
        printBanner("Size of Spam Words");
        System.out.println(spamWords.keySet().size());

        // b) Rebalance
        // Reasoning: If one word is not contained in one of the sets, the probability for P(H) (or P(S) for that matter) becomes 0
        rebalance(hamWords, spamWords, ALPHA);
        printBanner("Bottom Ham Words");
        showBottomNEntries(hamWords, DEBUG_ENTRIES);

        printBanner("Bottom Spam Words");
        showBottomNEntries(spamWords, DEBUG_ENTRIES);

        // c) Calibrate

        /**
         * // Select a top number of Spam Words to compare
        List<Map.Entry<String, Double>> topSpamWords = getTopNEntries(spamWords, topWordsToCompare);
        // Map the word to the number of occurences of all words first in spam, than in ham
        List<Double> probabilityToOccurInSpam = topSpamWords.stream().map(entry -> {
            return getProbabilityOfWordInMap(entry.getKey(), spamWords);
        }).toList();

        List<Double> probabilityToOccurInHam = topSpamWords.stream().map(entry -> {
            return getProbabilityOfWordInMap(entry.getKey(), hamWords);
        }).toList();**/

        // Read all HAM Mail
        int i = 0;
        int j = 0;
        Map<String, Double> validHam;
        for (File file : returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/valid/ham-kallibrierung")) {
            validHam = new HashMap<>();
            BigDecimal validHamProbabilityProduct = BigDecimal.ONE;
            BigDecimal validSpamProbabilityProduct = BigDecimal.ONE;
            readWordsAndPutCount(validHam, file);
            // For each word in the dataset, calculate the probability of the words being spam or ham
            for (Map.Entry<String, Double> e : validHam.entrySet()) {
                validHamProbabilityProduct = validHamProbabilityProduct.multiply(
                        getProbabilityOfWordInMap(e.getKey(), hamWords).multiply(BigDecimal.valueOf(e.getValue()))
                );

                validSpamProbabilityProduct = validSpamProbabilityProduct.multiply(
                        getProbabilityOfWordInMap(e.getKey(), spamWords).multiply(BigDecimal.valueOf(e.getValue()))
                );
            }
            BigDecimal spamProb = getSpamProbability(validSpamProbabilityProduct, validHamProbabilityProduct, BigDecimal.valueOf(S_THRESHOLD));
            if(spamProb.doubleValue() >= 0.5) {
                j++;
            }
            i++;
            printBanner(String.format("%s: %s", file.getName(), spamProb));
        }
        printBanner(String.format("%s out of %s Ham Mails where categorized as spam", j, i));

        // If Ham Mail failed with P(H) < 0.5, add the words to ham

        // Read all Spam Mail
        // If Spam Mail failed with P(S) < 0.5, add the words to spam
    }

    public static void readWordsAndPutCount(Map<String, Double> map, File file) {
        // https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
        // Read all files in dir
        if(!file.isDirectory()) {
            // https://stackoverflow.com/questions/4574041/read-next-word-in-java
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // Read line by line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String word : line.split(" ")) {
                    // Read word by word (Word is defined as everything split by a space)
                    // https://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java
                    map.merge(word, 1.0, Double::sum);
                }
            }
        }
    }

    public static List<File> returnListOfFilesInDir(String path) {
        File folder = new File(path);
        return Arrays.stream(folder.listFiles()).toList();
    }

    public static List<Map.Entry<String, Double>> getTopNEntries(Map<String, Double> map, int n) {
        return map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(n).toList();
    }

    public static void showTopNEntries(Map<String, Double> map, int n) {
        // https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
        getTopNEntries(map, n).forEach(stringIntegerEntry -> System.out.println(stringIntegerEntry));
    }

    public static List<Map.Entry<String, Double>> getBottomNEntries(Map<String, Double> map, int n) {
        return map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue()).limit(n).toList();
    }

    public static void showBottomNEntries(Map<String, Double> map, int n) {
       getBottomNEntries(map, n).forEach(stringIntegerEntry -> System.out.println(stringIntegerEntry));
    }

    public static void printBanner(String message) {
        System.out.println(String.format("------------- %s -------------", message));
    }
    
    public static void rebalance(Map<String, Double> map1, Map<String, Double> map2, double alpha) {
        for (String key : map1.keySet()) {
            if(!map2.containsKey(key)) {
                map2.put(key, alpha);
            }
        }

        for (String key : map2.keySet()) {
            if(!map1.containsKey(key)) {
                map1.put(key, alpha);
            }
        }
    }

    public static BigDecimal getProbabilityOfWordInMap(String word, Map<String, Double> map) {
        // https://stackoverflow.com/questions/39851350/reducing-map-by-using-java-8-stream-api
        double countOfWords = map.values().stream().reduce(0d, (a, b) -> a + b);
        System.out.println(countOfWords);
        if(map.containsKey(word)) {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(countOfWords), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ONE;
    }

    public static BigDecimal getSpamProbability(BigDecimal spamProb, BigDecimal hamProb, BigDecimal spamThreshold) {
        BigDecimal hamThreshold = BigDecimal.ONE.subtract(spamThreshold);
        // Divide the spam bias * spam values by (spam bias * spam values) + (ham bias * ham values)
        return (spamProb.multiply(spamThreshold).divide(
                        (spamProb.multiply(spamThreshold).add(hamProb.multiply(hamThreshold))),
                2, RoundingMode.HALF_UP));
    }

    public static double reduceListToProduct(List<Double> list) {
        return list.stream().reduce(1.0, (a, b) -> a * b);
    }
}
