import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Starter {
    public static void main(String[] args) {
        final double ALPHA = 0.5;
        final double S_THRESHOLD = 0.5;
        final int topWordsToCompare = 1000;
        final int DEBUG_ENTRIES = 15;

        printBanner("Start");
        // a) Read all files

        // Read all ham mails
        printBanner("Top Ham Words");
        Map<String, Double> hamWords = new HashMap<>();
        readWordsAndPutCount(hamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test");
        showTopNEntries(hamWords, DEBUG_ENTRIES);
        printBanner("Size of Ham Words");
        System.out.println(hamWords.keySet().size());

        // Read all spam mails
        printBanner("Top Spam Words");
        Map<String, Double> spamWords = new HashMap<>();
        readWordsAndPutCount(spamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test");
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
        // Select a top number of Spam Words to compare
        List<Map.Entry<String, Double>> topSpamWords = getTopNEntries(spamWords, topWordsToCompare);
        // Map the word to the number of occurences of all words first in spam, than in ham
        List<Double> probabilityToOccurInSpam = topSpamWords.stream().map(entry -> {
            return getRepresentationOfWordInMap(entry.getKey(), spamWords);
        }).toList();

        List<Double> probabilityToOccurInHam = topSpamWords.stream().map(entry -> {
            return getRepresentationOfWordInMap(entry.getKey(), hamWords);
        }).toList();
    }

    public static void readWordsAndPutCount(Map<String, Double> map, String path) {
        // https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
        // Read all files in dir
        File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            if(!fileEntry.isDirectory()) {
                // https://stackoverflow.com/questions/4574041/read-next-word-in-java
                Scanner scanner = null;
                try {
                    scanner = new Scanner(fileEntry);
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

    public static double getRepresentationOfWordInMap(String word, Map<String, Double> map) {
        // System.out.println(String.format("Word: %s, size: %s", word, map.entrySet().size()));
        return map.get(word) / map.entrySet().size();
    }

    public static double getProbabilityOfTopNWordsToBeContainedInSpam(double spamThreshold, List<Double> spamProbability, List<Double> hamProbability) {
        double hamThreshold = 1 - spamThreshold;
        // https://stackoverflow.com/questions/36833932/how-to-multiply-values-in-a-list-using-java-8-streams/36836942
        // P(S|topNWords)
        return spamThreshold * reduceListToProduct(spamProbability) / ((spamThreshold * reduceListToProduct(spamProbability)) + (hamThreshold * reduceListToProduct(hamProbability)));
    }

    public static double getProbabilityOfTopNWordsToBeContainedInHam(double spamThreshold, List<Double> spamProbability, List<Double> hamProbability) {
        double hamThreshold = 1 - spamThreshold;
        // https://stackoverflow.com/questions/36833932/how-to-multiply-values-in-a-list-using-java-8-streams/36836942
        // P(H|topNWords)
        return hamThreshold * reduceListToProduct(hamProbability) / ((spamThreshold * reduceListToProduct(spamProbability)) + (hamThreshold * reduceListToProduct(hamProbability)));
    }

    public static double reduceListToProduct(List<Double> list) {
        return list.stream().reduce(1.0, (a, b) -> a * b);
    }
}
