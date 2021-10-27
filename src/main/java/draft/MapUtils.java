package draft;

import java.util.List;
import java.util.Map;

public class MapUtils {
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

    public static void printBanner(String message) {
        System.out.println(String.format("------------- %s -------------", message));
    }
}
