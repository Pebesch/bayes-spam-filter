import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Starter {
    public static void main(String[] args) {
        // Read all ham mails
        Map<String, Integer> hamWords = new HashMap<>();
        readWordsAndPutCount(hamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test");
        showTopNEntries(hamWords, 20);
        // Read all spam mails
        Map<String, Integer> spamWords = new HashMap<>();
        readWordsAndPutCount(hamWords, "C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test");
        showTopNEntries(hamWords, 20);
    }

    public static void readWordsAndPutCount(Map<String, Integer> map, String path) {
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
                        map.merge(word, 1, Integer::sum);
                    }
                }
            }
        }
    }

    public static void showTopNEntries(Map<String, Integer> map, int n) {
        // https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
        map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(n).toList().forEach(stringIntegerEntry -> System.out.println(stringIntegerEntry));
    }
}
