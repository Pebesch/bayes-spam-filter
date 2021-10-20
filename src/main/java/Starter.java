import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Starter {
    public static void main(String[] args) {
        printBanner("Start");
        // a) Read all files

        // Read all ham mails
        printBanner("Top Ham Words");
        Map<String, Double> hamWords = new HashMap<>();
        readWordsAndPutCount(hamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test");
        showTopNEntries(hamWords, 5);

        // Read all spam mails
        printBanner("Top Spam Words");
        Map<String, Double> spamWords = new HashMap<>();
        readWordsAndPutCount(hamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test");
        showTopNEntries(hamWords, 5);

        // b) Rebalance
        // Reasoning: If one word is not contained in one of the sets, the probability for P(H) (or P(S) for that matter) becomes 0
        rebalance(hamWords, spamWords, 0.5);
        printBanner("Bottom Ham Words");
        showBottomNEntries(hamWords, 5);

        printBanner("Bottom Spam Words");
        showBottomNEntries(spamWords, 5);
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

    public static void showTopNEntries(Map<String, Double> map, int n) {
        // https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
        map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(n).toList().forEach(stringIntegerEntry -> System.out.println(stringIntegerEntry));
    }

    public static void showBottomNEntries(Map<String, Double> map, int n) {
        map.entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue()).limit(n).toList().forEach(stringIntegerEntry -> System.out.println(stringIntegerEntry));
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
}
