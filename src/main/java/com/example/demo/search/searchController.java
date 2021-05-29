package com.example.demo.search;

import com.example.demo.search.Helpers.Helpers;
import com.example.demo.search.indexing.Index;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
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
        }

        DatabaseQueryManager.closeConnection();
    }
    public static SiteInfo[] Search(String searchTerm) {
        if(searchTerm.startsWith("\"") && searchTerm.endsWith("\"")) {
            searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
            Index termIndex = new Index(searchTerm);
            var docsTokens = DatabaseQueryManager.getTokens(termIndex.GetTokens());
            var docsIdContainsSearchTerm = Helpers.getExactDocs(docsTokens, termIndex.GetTokens());
            var docs = DatabaseQueryManager.getDocsById(docsIdContainsSearchTerm);
            return docs.toArray(SiteInfo[]::new);
        }
        else {
            Index termIndex = new Index(searchTerm);
            var docsTokens = DatabaseQueryManager.getTokens(termIndex.GetTokens());
            var docsIdContainsSearchTerm = Helpers.getMostRelevantDocs(docsTokens, termIndex.GetTokens());
            var docs = DatabaseQueryManager.getDocsById(docsIdContainsSearchTerm);
            return docs.toArray(SiteInfo[]::new);
        }
    }
    @GetMapping
    public String index(@RequestParam(value = "search", defaultValue = "World") String search){
        String upperView = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" +
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
                "    <div class=\"content\">\n" +
                "        " + content + "\n" +
                "    </div>\n" +
                "</div></li>\n";
    }
}
