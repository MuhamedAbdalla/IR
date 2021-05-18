import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class main {

    public static void main(String[] args) throws InterruptedException {
        Map<String, Boolean> vis = new ConcurrentHashMap<>();
        Queue<String> vertexs = new LinkedList<>();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter number of site you want to start with");
        int numOfSites = scanner.nextInt();
        for (int i = 0; i < numOfSites; i++) {
            vertexs.add(scanner.next());
        }
        System.out.println("Enter Number of Thread you want to use");
        int numOfThreads = scanner.nextInt();
        if (numOfThreads > numOfSites) {
            System.exit(-1);
        }
        int[] threadCount = new int[numOfThreads];
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numOfSites; i++) {
            threadCount[i % numOfThreads]++;
        }
        for (int i = 0; i < numOfThreads; i++) {
            Queue<String> tmp = new LinkedList<>();
            for (int j = 0; j < threadCount[i]; j++) {
                tmp.add(vertexs.remove());
            }
            PageParser bfsObj = new PageParser(vis, tmp);
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
}
