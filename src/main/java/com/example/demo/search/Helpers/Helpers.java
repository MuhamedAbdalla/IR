package com.example.demo.search.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Helpers {
    public static String[] getMostRelevantDocs(HashMap<String, HashMap<String, ArrayList<Integer>>> docs, String tokens[]) {
        ArrayList<Pair<String, Integer>> tempDocs = new ArrayList();
        docs.forEach((doc_id, terms) -> {
            tempDocs.add(new Pair<String, Integer>(doc_id, getDocScore(terms, tokens)));
        });

        tempDocs.sort((o1, o2) -> o1.getSecond() - o2.getSecond());
        return tempDocs.stream().map( p -> p.getFirst()).toArray(String[]::new);
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
}
