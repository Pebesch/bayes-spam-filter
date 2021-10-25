import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class Starter {
    // https://www.youtube.com/watch?v=O2L2Uv9pdDA&ab_channel=StatQuestwithJoshStarmer
    public static void main(String[] args) {
        final double ALPHA = 0.001;
        final double S_THRESHOLD = 0.5;
        final int DEBUG_ENTRIES = 15;

        Utils.printBanner("Start");
        // a) Read all files

        // Read all ham mails
        Utils.printBanner("Top Ham Words");
        Map<String, Double> hamWords = new HashMap<>();
        for (File file : returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test")) {
            readWordsAndPutCount(hamWords, file);
        }
        MapUtils.showTopNEntries(hamWords, DEBUG_ENTRIES);
        Utils.printBanner("Size of Ham Words");
        System.out.println(hamWords.keySet().size());

        // Read all spam mails
        Utils.printBanner("Top Spam Words");
        Map<String, Double> spamWords = new HashMap<>();
        for (File file : returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test")) {
            readWordsAndPutCount(spamWords, file);
        }
        MapUtils.showTopNEntries(spamWords, DEBUG_ENTRIES);
        Utils.printBanner("Size of Spam Words");
        System.out.println(spamWords.keySet().size());

        // b) Rebalance
        // Reasoning: If one word is not contained in one of the sets, the probability for P(H) (or P(S) for that matter) becomes 0
        MapUtils.rebalance(hamWords, spamWords, ALPHA);
        Utils.printBanner("Bottom Ham Words");
        MapUtils.showBottomNEntries(hamWords, DEBUG_ENTRIES);

        Utils.printBanner("Bottom Spam Words");
        MapUtils.showBottomNEntries(spamWords, DEBUG_ENTRIES);

        // c) Calibrate

        // Read all HAM Mail
        int i = 0;
        int j = 0;
        Map<String, Double> validHam;
        List<File> files = returnListOfFilesInDir("C:/Repos/bayes-spam-filter/src/main/resources/valid/spam-kallibrierung");
        for (File file : files) {
            validHam = new HashMap<>();
            BigDecimal validHamProbabilityProduct = BigDecimal.ONE;
            BigDecimal validSpamProbabilityProduct = BigDecimal.ONE;
            readWordsAndPutCount(validHam, file);
            // For each word in the dataset, calculate the probability of the words being spam or ham
            for (Map.Entry<String, Double> e : validHam.entrySet()) {
                validHamProbabilityProduct = validHamProbabilityProduct.multiply(
                        BayesUtils.getProbabilityOfWordInMap(e.getKey(), hamWords).pow(e.getValue().intValue())
                );
                validSpamProbabilityProduct = validSpamProbabilityProduct.multiply(
                        BayesUtils.getProbabilityOfWordInMap(e.getKey(), spamWords).pow(e.getValue().intValue())
                );
            }
            BigDecimal spamProb = BayesUtils.getSpamProbability(validSpamProbabilityProduct, validHamProbabilityProduct, BigDecimal.valueOf(S_THRESHOLD));
            if(spamProb.doubleValue() >= 0.5) {
                j++;
            }
            i++;
            Utils.printBanner(String.format("%s of %s, %s: %s", i, files.size(), file.getName(), spamProb));
        }
        Utils.printBanner(String.format("%s out of %s Ham Mails where categorized as spam", j, i));
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
}
