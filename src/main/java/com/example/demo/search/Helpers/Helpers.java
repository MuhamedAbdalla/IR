package com.example.demo.search.Helpers;

import com.example.demo.search.DatabaseQueryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Helpers {
    public static HashMap<String, Integer> getMostRelevantDocs(HashMap<String, HashMap<String, ArrayList<Integer>>> docs, String tokens[]) {
        ArrayList<Pair<String, Integer>> tempDocs = new ArrayList();
        docs.forEach((doc_id, terms) -> {
            tempDocs.add(new Pair<String, Integer>(doc_id, getDocScore(terms, tokens)));
        });

        HashMap<String, Integer> docsScore = new HashMap<>();
        tempDocs.forEach(stringIntegerPair -> docsScore.computeIfAbsent(stringIntegerPair.getFirst(), s -> stringIntegerPair.getSecond()));
        return docsScore;
    }
    public static String[] getExactDocs(HashMap<String, HashMap<String, ArrayList<Integer>>> docs, String tokens[]) {
        ArrayList<String> tempDocs = new ArrayList();
        docs.forEach((doc_id, terms) -> {
            if(exactMatch(terms, tokens)) {
                tempDocs.add(doc_id);
            }
        });

        return tempDocs.toArray(String[]::new);
    }

    private static final int wordWeight = 100;
    private static final int initialScore = 1000000000;
    private static int getDocScore(HashMap<String, ArrayList<Integer>> docs, String tokens[]) {
        ArrayList<ArrayList<Integer>> tokensPos = new ArrayList<>();
        for(var token: tokens) {
            if(docs.containsKey(token)) {
                docs.get(token).sort((o1, o2) -> o1 - o2);
                tokensPos.add(docs.get(token));
            }
        }

        ArrayList<Integer> tempPos = new ArrayList<>();
        int minScore = initialScore;
        for(var pos: tokensPos.get(0)) {
            tempPos.add(pos);
            for(var i = 1; i < tokensPos.size(); i++) {
                pos = getClosestVal(tokensPos.get(i), tempPos);
                tempPos.add(pos);
            }
            tempPos.sort((o1, o2) -> o1 - o2);
            int tempScore = 0;
            int lastVal = tempPos.get(0);
            int badPos = 0;
            for(var val: tempPos) {
                int n = val - lastVal - 1;
                badPos += n == 0? 0 : 1;
                tempScore += badPos * n;
            }
            minScore = Math.min(minScore, tempScore);
            if(minScore == 0) {
                break;
            }
            tempPos.clear();
        }
        return minScore;
    }
    private static boolean exactMatch(HashMap<String, ArrayList<Integer>> docs, String tokens[]) {
        ArrayList<ArrayList<Integer>> tokensPos = new ArrayList<>();
        for(var token: tokens) {
            if(docs.containsKey(token)) {
                docs.get(token).sort((o1, o2) -> o1 - o2);
                tokensPos.add(docs.get(token));
            }
        }

        if(tokensPos.size() != tokens.length) {
            return false;
        }

        ArrayList<Integer> tempPos = new ArrayList<>();
        for(var pos: tokensPos.get(0)) {
            tempPos.add(pos);
            for(var i = 1; i < tokensPos.size(); i++) {
                if(Collections.binarySearch(tokensPos.get(i), tempPos.get(tempPos.size() - 1) + 1) >= 0) {
                    tempPos.add(tempPos.get(tempPos.size() - 1) + 1);
                }
                else {
                    break;
                }
            }
            if(tempPos.size() == tokens.length) {
                return true;
            }
            tempPos.clear();
        }
        return false;
    }

    private static int getClosestVal(ArrayList<Integer> values, ArrayList<Integer> set) {
        set.sort((o1, o2) -> o1 - o2);
        if(set.size() == 1) {
            int val = lowerBound(values, set.get(0));
            if(val > set.get(0)) {
                val = higherBound(values, set.get(0));
            }
            return val;
        }
        int dist = 1000000000, ans = -1;
        for(int i = 1; i < set.size(); i++) {
            int l = set.get(i - 1), r = set.get(i);
            if(l + 1 != r) {
                int val = lowerBound(values, r);
                if(val < set.get(i) && val != l && dist > Math.abs(l - val) + Math.abs(r - val)) {
                    dist = Math.abs(l - val) + Math.abs(r - val);
                    ans = val;
                }
            }
        }

        if(ans == -1) {
            ans = higherBound(values, set.get(set.size() - 1));
        }

        return ans;
    }

    public static int lowerBound(ArrayList<Integer> arr, int val) {
        int l = 0, r = arr.size() - 1;
        int ans = val;
        while(l <= r) {
            int mid = (l + r) / 2;
            if(arr.get(mid) < val) {
                ans = val;
                l = mid + 1;
            }
            else {
                r = mid - 1;
            }
        }

        return ans;
    }

    public static int higherBound(ArrayList<Integer> arr, int val) {
        int l = 0, r = arr.size() - 1;
        int ans = val - 1;
        while(l <= r) {
            int mid = (l + r) / 2;
            if(arr.get(mid) > val) {
                ans = val;
                l = mid + 1;
            }
            else {
                r = mid - 1;
            }
        }

        return ans;
    }

    public static String applyKGram(String word) {
        return applyKGram(word, 2);
    }

    public static String applyKGram(String word, int k) {
        ArrayList<String> kGrams = new ArrayList<>();
        for(int i = 0; i < word.length() - k + 1; i++) {
            kGrams.add(word.substring(i, i + k));
        }
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
        if(word.length() > 0) {
            mergeListWithMap(wordCount, DatabaseQueryManager.getPureTokensStartsWith(word.substring(0, 1)));
            if(word.length() > 1) {
                mergeListWithMap(wordCount, DatabaseQueryManager.getPureTokensEndsWith(word.substring(word.length() - 1, word.length())));
            }
        }
        for(var kgram: kGrams) {
            mergeListWithMap(wordCount, DatabaseQueryManager.getPureTokensContains(kgram));
        }
        HashMap<String, Float> wordValue = new HashMap<>();
        int numberOfKGrams = word.length() - k + 1;
        wordCount.forEach((str, val) ->{
            float count = val;
            float numberOfStrKgrams = str.length() - k + 1;
            wordValue.put(str, count / (numberOfKGrams + numberOfStrKgrams - count));
        });
        return wordValue.keySet().stream().max((word1, word2) -> {
            float val = wordValue.get(word2) - wordValue.get(word1);
            if(val < 0) {
                return -1;
            }
            else if (val > 0) {
                return 1;
            }
            return 0;

        }).orElse("");
    }

    private static void mergeListWithMap(HashMap<String, Integer> wordCount, String words[]) {
        Arrays.stream(words).forEach(s -> {
            wordCount.computeIfAbsent(s, s1 -> 0);
            wordCount.put(s, wordCount.get(s) + 1);
        });
    }

    public static String applySoundex(String word) {
        if(word.length() <= 1) {
            return word;
        }
        word = word.toLowerCase();
        word = word.substring(0, 1) + mapCharsToNums(word.substring(1));
        word = removeConsqRepetitiveChars(word);
        word = removeChars(word, '0');
        return makeWordLength(word, 4);
    }

    private static String mapCharsToNums(String word) {
        String ans = "";
        String zeroSet = "aeiouhwy";
        String oneSet = "bfpv";
        String twoSet = "cgjkqsxz";
        String threeSet = "dt";
        String fourSet = "l";
        String fiveSet = "mn";
        String sixSet = "r";
        for(var ch: word.toCharArray()) {
            if(zeroSet.indexOf(ch) != -1) {
                ans += "0";
            }
            else if(oneSet.indexOf(ch) != -1) {
                ans += "1";
            }
            else if(twoSet.indexOf(ch) != -1) {
                ans += "2";
            }
            else if(threeSet.indexOf(ch) != -1) {
                ans += "3";
            }
            else if(fourSet.indexOf(ch) != -1) {
                ans += "4";
            }
            else if(fiveSet.indexOf(ch) != -1) {
                ans += "5";
            }
            else if(sixSet.indexOf(ch) != -1) {
                ans += "6";
            }
        }
        return ans;
    }

    private static String removeConsqRepetitiveChars(String word) {
        String ans = word.substring(0, 1);
        for(int i = 1; i < word.length(); i++) {
            if(word.charAt(i - 1) != word.charAt(i)) {
                ans += word.charAt(i);
            }
        }
        return ans;
    }

    private static String removeChars(String word, char badChar) {
        String ans = "";
        for(var ch: word.toCharArray()) {
            if(ch != badChar) {
                ans += ch;
            }
        }

        return ans;
    }

    private static String makeWordLength(String word, int len) {
        if(word.length() == len) {
            return word;
        }
        else if(word.length() > len) {
            return word.substring(0, len);
        }
        else {
            return word + "0".repeat(len - word.length());
        }
    }
}
