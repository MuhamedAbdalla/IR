import indexing.Index;
import org.jsoup.Connection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class main {

    public static void main(String[] args)  {
       RunIddexing();
    }

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
    public static void RunIddexing() {
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
}
