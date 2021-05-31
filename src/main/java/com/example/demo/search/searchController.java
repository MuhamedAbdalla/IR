package com.example.demo.search;

import com.example.demo.search.Helpers.Helpers;
import com.example.demo.search.Helpers.Pair;
import com.example.demo.search.indexing.Index;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@RestController
@RequestMapping(path = "/search")
public class searchController {
    public static void RunCrawler() throws InterruptedException {
        Map<String, Boolean> vis = new ConcurrentHashMap<>();
        Queue<String> vertexs = new ConcurrentLinkedDeque<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of site you want to start with");
        int numOfSites = scanner.nextInt();
        for (int i = 0; i < numOfSites; i++) {
            vertexs.add(scanner.next());
        }

        System.out.println("Enter Number of Thread you want to use");
        int numOfThreads = scanner.nextInt();
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numOfThreads; i++) {
            PageParser bfsObj = new PageParser(vis, vertexs);
            Thread thread = new Thread(bfsObj);
            thread.setName("" + (i + 1));
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.start();
        }

        boolean finish = false;
        while (!finish) {
            Thread.sleep(250);
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    finish = false;
                    break;
                }
                else {
                    finish = true;
                }
            }
        }
    }
    public static void RunIndexing() {
        ArrayList<SiteInfo> docs = DatabaseQueryManager.getAllDocs();
        ArrayList<Index> indices = new ArrayList<>();
        for(SiteInfo doc: docs) {
            indices.add(new Index(doc.getContent()));
        }

        for(int i = 0; i < docs.size(); i++) {
            var tokens = indices.get(i).GetTokens();
            for(int j = 0; j < tokens.length; j++) {
                if(!tokens[j].isEmpty()) {
                    DatabaseQueryManager.insertIntoTerms(tokens[j], docs.get(i).getId(), j);
                }
            }
            tokens = indices.get(i).GetTokensBeforeStemming();
            for(int j = 0; j < tokens.length; j++) {
                if(!tokens[j].isEmpty()) {
                    DatabaseQueryManager.insertIntoTermNotStemming(tokens[j], docs.get(i).getId());
                }
            }
        }

        DatabaseQueryManager.closeConnection();
    }

    private static void getAllCompinations(String[] terms, HashMap<String, HashSet<String>> hashWords, ArrayList<String> container, int limit, int idx, ArrayList<String> res) {
        if(idx == terms.length) {
            container.add(String.join(" ", res));
            return;
        }
        if(hashWords.containsKey(terms[idx])) {
            for(var str: hashWords.get(terms[idx])) {
                if(container.size() == limit) {
                    break;
                }
                res.add(str);
                getAllCompinations(terms,hashWords,container, limit, idx + 1, res);
                res.remove(res.size() - 1);
            }
        }
        else {
            getAllCompinations(terms,hashWords,container, limit, idx + 1, res);
        }
    }

    private static HashMap<String, Pair<SiteInfo, Integer>> getDocsThatMatch(String searchTerm) {
        HashMap<String, Pair<SiteInfo, Integer>> res = new HashMap<>();
        if(searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
            searchTerm = String.join(" ", Arrays.stream(searchTerm.split(" ")).
                    map(s -> Helpers.applyKGram(s)).toArray(String[]::new));
            Index termIndex = new Index(searchTerm);
            var docsTokens = DatabaseQueryManager.getTokens(termIndex.GetTokens());
            var docsIdContainsSearchTerm = Helpers.getExactDocs(docsTokens, termIndex.GetTokens());
            var docs = DatabaseQueryManager.getDocsById(docsIdContainsSearchTerm);
            docs.forEach(siteInfo -> res.computeIfAbsent(String.valueOf(siteInfo.getId()), s -> new Pair<>(siteInfo, 0)));
        }
        else {
            searchTerm = String.join(" ", Arrays.stream(searchTerm.split(" ")).
                    map(s -> Helpers.applyKGram(s)).toArray(String[]::new));
            Index termIndex = new Index(searchTerm);
            var docsTokens = DatabaseQueryManager.getTokens(termIndex.GetTokens());
            var docsIdContainsSearchTerm = Helpers.getMostRelevantDocs(docsTokens, termIndex.GetTokens());
            var docs = DatabaseQueryManager.getDocsById(docsIdContainsSearchTerm.keySet().toArray(String[]::new));
            docs.forEach(siteInfo -> res.computeIfAbsent(String.valueOf(siteInfo.getId()), s -> new Pair<>(siteInfo, docsIdContainsSearchTerm.get(String.valueOf(siteInfo.getId())))));
        }
        return res;
    }

    public static SiteInfo[] Search(String term) {
        boolean exact = term.startsWith("\"") && term.endsWith("\"");
        if (exact) {
            term = term.substring(1, term.length() - 1);
        }
        var terms = term.toLowerCase().split(" ");
        var hashWords =  DatabaseQueryManager.getAllTermsWithNoStemming();
        ArrayList<String> searchTerms = new ArrayList<>();
        var termshash = Arrays.stream(terms).map(s -> Helpers.applySoundex(s)).toArray(String[]::new);
        getAllCompinations(termshash, hashWords, searchTerms, 10, 0, new ArrayList<>());
        HashMap<String, Pair<SiteInfo, Integer>> docs = new HashMap<>();
        for(var searchTerm: searchTerms) {
            if (exact) {
                searchTerm = "\"" + searchTerm + "\"";
            }
           var tempDocs = getDocsThatMatch(searchTerm);
           tempDocs.forEach((s, siteInfoIntegerPair) -> {
               docs.computeIfAbsent(s, s1 -> new Pair<>(siteInfoIntegerPair.getFirst(), 0));
               docs.get(s).setSecond(docs.get(s).getSecond() + siteInfoIntegerPair.getSecond());
           });
        }
        ArrayList<SiteInfo> res = new ArrayList<>();
        docs.forEach((s, siteInfoIntegerPair) -> res.add(siteInfoIntegerPair.getFirst()));
        res.sort((o1, o2) -> docs.get(String.valueOf(o1.getId())).getSecond() - docs.get(String.valueOf(o2.getId())).getSecond());
        return res.toArray(SiteInfo[]::new);
    }

    @GetMapping
    public String index(@RequestParam(value = "search", defaultValue = "World") String search){
        String upperView = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" +
                "<style> .text {\n" +
                "   overflow: hidden;\n" +
                "   text-overflow: ellipsis;\n" +
                "   display: -webkit-box;\n" +
                "   -webkit-line-clamp: 4; \n" +
                "   -webkit-box-orient: vertical;\n" +
                "} </style>" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<ul style=\"list-style-type:none;\">";
        String lowView = "</ul>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";
        String searchView = "";
        SiteInfo []docs = Search(search);
        for(var doc: docs) {
            String url = doc.getUrl();
            String content = doc.getContent();
            searchView += createView(url, content);
        }
        return upperView
                + searchView
                + lowView;
    }

    private String createView(String url, String content) {
        return "<li><div class=\"container\">\n" +
                "    <div class=\"url\">\n" +
                "        <a href=\"" + url + "\">" + url + "</a>\n" +
                "    </div>\n" +
                "    <div class=\"content text\">\n" +
                "        " + content + "\n" +
                "    </div>\n" +
                "</div></li>\n";
    }
}
