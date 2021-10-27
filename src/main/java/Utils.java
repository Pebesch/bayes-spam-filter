import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;

public class Utils {
    public static void printBanner(String message) {
        System.out.println(String.format("------------- %s -------------", message));
    }

    public static void printResult(Double[] arr, Double S_THRESHOLD, Double ALPHA) {
        Utils.printBanner(String.format("Runs: %s, ham: %2.3f, spam: %2.3f, threshold: %s, alpha: %s", arr[0], arr[1], arr[2], S_THRESHOLD, ALPHA));
    }

    /**
     * Maps Word::Count
     * @param map the map to fill
     * @param file the origin
     */
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

    /**
     * Puts all words in a list
     * @param list list to fill
     * @param file origin
     */
    public static void readWordsAndPutToList(List<String> list, File file) {
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
                    list.add(word);
                }
            }
        }
    }

    public static List<File> returnListOfFilesInDir(String path) {
        File folder = new File(path);
        return Arrays.stream(folder.listFiles()).toList();
    }

    public static Double[] run(List<File> files, Double S_THRESHOLD, Map<String, Double> hamMap, Map<String, Double> spamMap) {
        Double i = 0.0;
        Double ham = 0.0;
        Double spam = 0.0;
        // For each file
        for (File file : files) {
            // Fill the list of words
            List<String> words = new ArrayList();
            BigDecimal hamProbabilityProduct = BigDecimal.ONE;
            BigDecimal spamProbabilityProduct = BigDecimal.ONE;
            Utils.readWordsAndPutToList(words, file);

            for (String word : words) {
                // For each word in the dataset multiply the product by P(W_i|H)
                hamProbabilityProduct = hamProbabilityProduct.multiply(
                        BigDecimal.valueOf(BayesUtils.getProbabilityOfWordInMap(word, hamMap))
                );
                // For each word in the dataset multiply the product by P(W_i|S)
                spamProbabilityProduct = spamProbabilityProduct.multiply(
                        BigDecimal.valueOf(BayesUtils.getProbabilityOfWordInMap(word, spamMap))
                );
            }
            BigDecimal spamProb = BayesUtils.getSpamProbability(hamProbabilityProduct, spamProbabilityProduct);
            if(spamProb.doubleValue() > S_THRESHOLD) {
                spam++;
            } else {
                ham++;
            }
            Utils.printBanner(String.format("%s of %s, %s: %2.3f", i, files.size(), file.getName(), spamProb));
            i++;
        }
        return new Double[] {Double.valueOf(files.size()), ham, spam};
    }
}
