public class SiteInfo {
    private String url;
    private String content;

    public SiteInfo(String url, String content) {
        this.url = url;
        this.content = content;
    }

    public String getUrl() {
        return this.url;
    }

    public String getContent() {
        return this.content;
    }
}
