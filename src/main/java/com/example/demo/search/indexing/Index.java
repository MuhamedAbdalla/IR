package com.example.demo.search.indexing;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Index {
    private static String REGEX_TOKEN = "[\\W]"; // non alphanumeric
    private static String[] ENGLISH_STOPWORDS;
    private String[] tokens, tokensBeforeStemming;
    public Index(String str) {
        try {
            ENGLISH_STOPWORDS = new String(Files.readAllBytes(Paths.get("src/main/java/com/example/demo/search/indexing/english-stopwords.txt").toAbsolutePath())).split("\n");
        }catch (Exception exc) {
            System.out.println(exc);
            ENGLISH_STOPWORDS = new String[0];
        }

        tokens = Tokenize(str);
        tokens = CaseFolding(tokens); // TODO: save these tokens
        tokensBeforeStemming = RemoveStopwords(tokens);
        tokens = Stem(tokensBeforeStemming);
    }

    private static void debug(String[] str) {
        for (var word: str) {
            System.out.println(word);
        }
        System.out.println();
    }

    private static String[] Tokenize(String str) {
        var tokens = str.split(REGEX_TOKEN);
        return Arrays.stream(tokens).filter(token -> !token.isEmpty()).toArray(String[]::new);
    }

    private static String[] CaseFolding(String[] tokens) {
        return Arrays.stream(tokens).map( token -> token.toLowerCase()).toArray(String[]::new);
    }

    private static String[] RemoveStopwords(String[] tokens) {
        return Arrays.stream(tokens)
                .map(str -> Arrays.stream(ENGLISH_STOPWORDS).anyMatch(str::equals)? "" : str).toArray(String[]::new);
    }

    private static String[] Stem(String[] tokens) {
        PorterStemmer stemmer = new PorterStemmer();
        return Arrays.stream(tokens).map(token -> stemmer.stem(token)).toArray(String[]::new);
    }

    public String[] GetTokens() {
        return tokens;
    }

    public String[] GetTokensBeforeStemming() {
        return tokensBeforeStemming;
    }
}
