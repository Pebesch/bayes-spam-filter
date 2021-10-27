import java.io.File;
import java.util.*;

public class Starter {

    // https://www.youtube.com/watch?v=O2L2Uv9pdDA&ab_channel=StatQuestwithJoshStarmer
    public static void main(String[] args) {
        final double ALPHA = 0.001;
        final double S_THRESHOLD = 0.5;

        final String anlernHam = "C:/Repos/bayes-spam-filter/src/main/resources/train/ham-anlern";
        final String anlernSpam = "C:/Repos/bayes-spam-filter/src/main/resources/train/spam-anlern";
        final String kallibrierungHam = "C:/Repos/bayes-spam-filter/src/main/resources/valid/ham-kallibrierung";
        final String kallibrierungSpam = "C:/Repos/bayes-spam-filter/src/main/resources/valid/spam-kallibrierung";
        final String testHam = "C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test";
        final String testSpam = "C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test";

        Utils.printBanner("Start");
        // a) Read all files

        // Read all ham mails
        Map<String, Double> hamWords = new HashMap<>();
        for (File file : Utils.returnListOfFilesInDir(anlernHam)) {
            Utils.readWordsAndPutCount(hamWords, file);
        }

        // Read all spam mails
        Map<String, Double> spamWords = new HashMap<>();
        for (File file : Utils.returnListOfFilesInDir(anlernSpam)) {
            Utils.readWordsAndPutCount(spamWords, file);
        }

        // b) Rebalance
        // Reasoning: If one word is not contained in one of the sets, the probability for P(H) (or P(S) for that matter) becomes 0
        MapUtils.rebalance(hamWords, spamWords, ALPHA);

        // c) Calibrate


        // d) Run Test and print result
        // Read all HAM Mail
        Double[] hamRun = Utils.run(Utils.returnListOfFilesInDir(testHam), S_THRESHOLD, hamWords, spamWords);
        Utils.printResult(hamRun, S_THRESHOLD, ALPHA);

        // Read all SPAM Mail
        Double[] spamRun = Utils.run(Utils.returnListOfFilesInDir(testSpam), S_THRESHOLD, hamWords, spamWords);
        Utils.printResult(spamRun, S_THRESHOLD, ALPHA);
    }
}
