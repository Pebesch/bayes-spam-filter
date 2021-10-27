package draft;

public class Starter {
    public static final String anlernHam = "C:/Repos/bayes-spam-filter/src/main/resources/train/ham-anlern";
    public static final String anlernSpam = "C:/Repos/bayes-spam-filter/src/main/resources/train/spam-anlern";
    public static final String kallibrierungHam = "C:/Repos/bayes-spam-filter/src/main/resources/valid/ham-kallibrierung";
    public static final String kallibrierungSpam = "C:/Repos/bayes-spam-filter/src/main/resources/valid/spam-kallibrierung";
    public static final String testHam = "C:/Repos/bayes-spam-filter/src/main/resources/test/ham-test";
    public static final String testSpam = "C:/Repos/bayes-spam-filter/src/main/resources/test/spam-test";

    public static void main(String[] args) {
        double alpha = 0.01;
        double threshold = 0.95;

        BayesSpamFilter filter = new BayesSpamFilter(alpha, threshold);
    }
}
