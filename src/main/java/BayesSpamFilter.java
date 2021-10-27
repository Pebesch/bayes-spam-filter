import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class BayesSpamFilter {
    private Map<String, Double> learnedHamMap = new HashMap<>();
    private Map<String, Double> learnedSpamMap = new HashMap<>();

    public BayesSpamFilter(double alpha, double threshold) {
        // Anlernphase
        MapUtils.printBanner("Anlernphase");
        learnFolder(Starter.anlernHam, learnedHamMap);
        learnFolder(Starter.anlernSpam, learnedSpamMap);
        // Rebalancing
        MapUtils.printBanner("Rebalancing");
        MapUtils.rebalance(learnedHamMap, learnedSpamMap, alpha);
        // Run Calibration
        MapUtils.printBanner("Kalibrierung");
        runFolder(Starter.kallibrierungHam, threshold);
        runFolder(Starter.kallibrierungSpam, threshold);
        // Run Test
        MapUtils.printBanner("Test");
        runFolder(Starter.testHam, threshold);
        runFolder(Starter.testSpam, threshold);
        System.out.println(String.format("Used alpha: %s, used spamThreshold: %s", alpha, threshold));
    }

    // Learns the contents of a whole folder
    // Puts the result in a map Word::Occurrence
    private int learnFolder(String path, Map<String, Double> target) {
        int i = 0;
        File folder = new File(path);
        // Go through each file
        for(File file : Arrays.stream(folder.listFiles()).toList()) {
            if(!file.isDirectory()) {
                // https://stackoverflow.com/questions/4574041/read-next-word-in-java
                Scanner scanner = null;
                try {
                    scanner = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // Read line by line and put all words in a map
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    for (String word : line.split(" ")) {
                        // Read word by word (Word is defined as everything split by a space)
                        // https://stackoverflow.com/questions/81346/most-efficient-way-to-increment-a-map-value-in-java
                        if(!word.equals("")) {
                            target.merge(word, 1.0, Double::sum);
                        }
                    }
                }
            }
            i++;
        }
        return i;
    }

    // Runs a folder
    private void runFolder(String path, double threshold) {
        File folder = new File(path);
        int ham = 0;
        int i = 0;
        for(File file : Arrays.stream(folder.listFiles()).toList()) {
            ham += runFile(file, threshold) ? 1 : 0;
            i++;
        }
        // Log the relevant data
        System.out.println(String.format("(%4.2f) %s of %s Files in %s were classified as ham.", ((double)ham/i*100),ham, i, path));
    }

    // Runs for each file
    private boolean runFile(File file, double threshold) {
        // Read all words in the mail
        List<String> words = new ArrayList<>();
        fileContentsToList(file, words);
        // Map all Word::Occurrences to Word::ChanceToOccurInMap
        Map<String, Double> probabilityHamMapPerWord = mapToProbabilityToOccur(learnedHamMap);
        Map<String, Double> probabilitySpamMapPerWord = mapToProbabilityToOccur(learnedSpamMap);
        // Multiply all P(w|[S/H]) where w stands for word
        BigDecimal hamProb = BigDecimal.ONE;
        BigDecimal spamProb = BigDecimal.ONE;
        for(String word : words) {
            if(probabilityHamMapPerWord.containsKey(word)) hamProb = hamProb.multiply(BigDecimal.valueOf(probabilityHamMapPerWord.get(word)));
            if(probabilitySpamMapPerWord.containsKey(word)) spamProb = spamProb.multiply(BigDecimal.valueOf(probabilitySpamMapPerWord.get(word)));
        }

        // Formula taken from https://www.youtube.com/watch?v=O2L2Uv9pdDA&ab_channel=StatQuestwithJoshStarmer
        BigDecimal spamThreshold = BigDecimal.valueOf(threshold);
        BigDecimal hamThreshold = BigDecimal.ONE.subtract(spamThreshold);
        BigDecimal denominator = (spamProb.multiply(spamThreshold)).add((hamProb).multiply(hamThreshold));
        BigDecimal hamProbabilityForMessage = (hamProb.multiply(hamThreshold)).divide(denominator, MathContext.DECIMAL128);
        BigDecimal spamProbabilityForMessage = (spamProb.multiply(spamThreshold)).divide(denominator, MathContext.DECIMAL128);
        // Compare P(H|m) with P(S|m) where m stands for message
        return hamProbabilityForMessage.doubleValue() > spamProbabilityForMessage.doubleValue();
    }

    private void fileContentsToList(File file, List<String> target) {
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
                    target.add(word);
                }
            }
        }
    }

    // Example "the" in ham, occurs 9016 times / total number of occurring words 463226.00030102814
    // Equals the=0.019463501604272942
    // Returns a map that contains P(w|[H / S])
    private Map<String, Double> mapToProbabilityToOccur(Map<String, Double> map) {
        double occurencesInMap = getOccurrencesInMapCnt(map);
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> e : map.entrySet()) {
            result.put(e.getKey(), e.getValue() / occurencesInMap);
        }
        return result;
    }

    // Calculate the total weight of messages
    private double getOccurrencesInMapCnt(Map<String, Double> map) {
        double cnt = 0;
        for (Map.Entry<String, Double> e : map.entrySet()) {
            cnt += e.getValue();
        }
        return cnt;
    }
}
