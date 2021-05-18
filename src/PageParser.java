import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

public class PageParser implements Runnable {
    private final int MAX_DOC = 3000;
    private DatabaseQueryManager db;
    private static int doc_count = 0;
    private Queue<String> vertexs;
    private Map<String, Boolean> vis;

    public PageParser(Map<String, Boolean> vis, Queue<String> vertexs) {
        db = new DatabaseQueryManager();
        this.vis = vis;
        this.vertexs = vertexs;
    }

    public void run() {
        while (!vertexs.isEmpty() && doc_count <= MAX_DOC) {
            String url = vertexs.remove();
            try {
                getUrlContent(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValid(String url){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .timeout(10000)
                    .execute();
            return response.statusCode() == 200;
        } catch (IOException e) {
            System.out.println("io - " + e);
        }
        return false;
    }

    // DFS implementation for crawling
    private void getUrlContent(String url) throws IOException {
        if (doc_count > MAX_DOC) System.exit(0);
        if (vis.containsKey(url) || !isValid(url)) {
            return;
        }
        vis.put(url, true);
        Document doc = Jsoup.connect(url).get();
        String content = doc.body().text();
        if (doc.select("html").first().attr("lang").equals("en") ||
                doc.select("html").first().attr("lang").equals("")) {
            // insert data to SQL database
            SiteInfo siteInfo = new SiteInfo(url, content);
            doc_count++;
            db.insert(siteInfo);
        }
        // fetching all urls then recursive call
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            vertexs.add(link.attr("abs:href"));
        }
    }
}
