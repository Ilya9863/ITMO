package info.kgeorgiy.ja.kurkin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloads;
    private final ExecutorService extractors;
    private final int perHost;

    private final Map<String, Semaphore> semaphoreMap;

    /**
     * default constructor WebCrawler
     *
     * @param downloader  allows you to download pages and extract links from them;
     * @param downloaders maximum number of simultaneously loaded pages;
     * @param extractors  the maximum number of pages from which links are extracted at the same time;
     * @param perHost     maximum number of pages loaded simultaneously from one host
     */

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloads = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.semaphoreMap = new ConcurrentHashMap<>();
        this.perHost = perHost;
    }

    private void WorkerWithLevelUrl(String url, Set<String> pullWithTasks, Map<String, IOException> errors,
                                    Set<String> tasksForDownload, Phaser blockOnLevelUrl, int depth) {
        try {
            URLUtils.getURI(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return;
        }
        blockOnLevelUrl.register();
        Runnable pullTasks = () -> {
            String host;
            try {
                host = URLUtils.getHost(url);
                semaphoreMap.putIfAbsent(host, new Semaphore(perHost));
            } catch (MalformedURLException e) {
                errors.put(url, e);
                return;
            }
            try {
                semaphoreMap.get(host).acquire();
                Document doc = downloader.download(url);
                tasksForDownload.add(url);
                if (depth == 1) return;
                blockOnLevelUrl.register();
                Runnable pullForExtracting = () -> {
                    try {
                        List<String> extractLinks = doc.extractLinks();
                        pullWithTasks.addAll(extractLinks);
                    } catch (IOException e) {
                        errors.put(url, e);
                    } finally {
                        blockOnLevelUrl.arrive();
                    }
                };
                extractors.submit(pullForExtracting);
            } catch (IOException e) {
                errors.put(url, e);
            } catch (InterruptedException ignored) {}
            finally {
                blockOnLevelUrl.arrive();
                semaphoreMap.get(host).release();
            }
        };
        downloads.submit(pullTasks);
    }

    /**
     * Downloads web site up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> successDownload = ConcurrentHashMap.newKeySet();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> pullWithTasks = ConcurrentHashMap.newKeySet();
        pullWithTasks.add(url);
        for (int i = 0; i < depth; i++) {
            Phaser blockOnLevelUrl = new Phaser(1);
            int levelSize = pullWithTasks.size();
            List<String> levelPull = new ArrayList<>(pullWithTasks);
            pullWithTasks.clear();
            for (int j = 0; j < levelSize; j++) {
                if (visited.add(levelPull.get(j))) {
                    WorkerWithLevelUrl(levelPull.get(j), pullWithTasks, errors, successDownload, blockOnLevelUrl, depth - i);
                }
            }
            blockOnLevelUrl.arriveAndAwaitAdvance();
        }
        return new Result(new ArrayList<>(successDownload), errors);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        extractors.shutdownNow();
        downloads.shutdownNow();
    }

    private static int SetBaseOrCustomParamForMain(String[] args, int numberOfParam) {
        return (args.length > numberOfParam) ? Integer.parseInt(args[numberOfParam]) : 1;
    }

    /**
     * runs a crawl from the command line:
     * WebCrawler url [depth [downloads [extractors [perHost]]]]
     *
     * @param args: depth
     *              downloads
     *              extractors
     *              perHost
     *              If one of the parameters is not entered, the default value of 1 will be set
     */

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            System.err.println("Doesn't match the format: WebCrawler url [depth [downloads [extractors [perHost]]]]");
        } else {
            int depth, downloads, extractors, perHost;
            depth = SetBaseOrCustomParamForMain(args, 1);
            downloads = SetBaseOrCustomParamForMain(args, 2);
            extractors = SetBaseOrCustomParamForMain(args, 3);
            perHost = SetBaseOrCustomParamForMain(args, 4);

            try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(0.1), downloads, extractors, perHost)) {
                var res = webCrawler.download(args[0], depth);
                System.out.println(res);
            } catch (IOException e) {
                System.out.println("Can't create downloader: " + e);
            }
        }
    }

}

