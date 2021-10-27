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

    public static Double[] run(List<File> files, Double S_THRESHOLD) {
        Double i = 0.0;
        Double ham = 0.0;
        Double spam = 0.0;
        for (File file : files) {
            Map<String, Double> map = new HashMap<>();
            BigDecimal validHamProbabilityProduct = BigDecimal.ONE;
            BigDecimal validSpamProbabilityProduct = BigDecimal.ONE;
            Utils.readWordsAndPutCount(map, file);
            // For each word in the dataset, calculate the probability of the words being spam or ham
            for (Map.Entry<String, Double> e : map.entrySet()) {
                validHamProbabilityProduct = validHamProbabilityProduct.multiply(
                        BayesUtils.getProbabilityOfWordInMap(e.getKey(), map).pow(e.getValue().intValue())
                );
                validSpamProbabilityProduct = validSpamProbabilityProduct.multiply(
                        BayesUtils.getProbabilityOfWordInMap(e.getKey(), map).pow(e.getValue().intValue())
                );
            }
            BigDecimal spamProb = BayesUtils.getSpamProbability(validSpamProbabilityProduct, validHamProbabilityProduct, BigDecimal.valueOf(S_THRESHOLD));
            if(spamProb.doubleValue() > S_THRESHOLD) {
                spam++;
            } else {
                ham++;
            }
            Utils.printBanner(String.format("%s of %s, %s: %2.3f", i, files.size(), file.getName(), spamProb.multiply(BigDecimal.valueOf(100))));
            i++;
        }
        return new Double[] {Double.valueOf(files.size()), ham, spam};
    }
}
