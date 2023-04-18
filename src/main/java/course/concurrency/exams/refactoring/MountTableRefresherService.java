package course.concurrency.exams.refactoring;

import course.concurrency.exams.refactoring.Others.MountTableManagerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    private MountTableManagerFactory mountTableManagerFactory;

    public void setMountTableManagerFactory(MountTableManagerFactory mountTableManagerFactory) {
        this.mountTableManagerFactory = mountTableManagerFactory;
    }

    //load should be measured and then swap to fixed thread pool
    private ExecutorService mountTableRefresherExecutor = Executors.newCachedThreadPool(
            new MountTableRefresherThreadFactory()
    );

    public void serviceInit() {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh() {
        List<Others.RouterState> cachedRecords = routerStore.getCachedRecords();
        List<MountTableRefresher> refreshTasks = new ArrayList<>();
        for (Others.RouterState routerState : cachedRecords) {
            String adminAddress = routerState.getAdminAddress();
            if (adminAddress == null || adminAddress.length() == 0) {
                // this router has not enabled router admin.
                continue;
            }
            if (isLocalAdmin(adminAddress)) {
                /*
                 * Local router's cache update does not require RPC call, so no need for
                 * RouterClient
                 */
                refreshTasks.add(getLocalRefresher(adminAddress));
            } else {
                refreshTasks.add(
                        new MountTableRefresher(
                                mountTableManagerFactory.create(adminAddress), adminAddress
                        ));
            }
        }
        if (!refreshTasks.isEmpty()) {
            invokeRefresh(refreshTasks);
        }
    }

    protected MountTableRefresher getLocalRefresher(String adminAddress) {
        return new MountTableRefresher(mountTableManagerFactory.create("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresher> refreshTasks) {
        // submit all tasks
        var tasks = refreshTasks.stream()
                .map(this::mapToCompletableFuture)
                .toArray(CompletableFuture[]::new);

        //wait all threads complete
        CompletableFuture.allOf(tasks).join();

        var results = Arrays.stream(tasks)
                .map(e -> {
                    try {
                        return (RefreshResult) e.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());
        if (results.stream().anyMatch(RefreshResult::isTimedOut)) {
            log("Not all router admins updated their cache");
        }
        logResult(results);
    }

    private CompletableFuture<RefreshResult> mapToCompletableFuture(MountTableRefresher task) {
        return CompletableFuture.supplyAsync(task, mountTableRefresherExecutor)
                .completeOnTimeout(
                        new RefreshResult(task.getAdminAddress(), false, true),
                        cacheUpdateTimeout,
                        TimeUnit.MILLISECONDS
                )
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof InterruptedException) {
                        log("Mount table cache refresher was interrupted.");
                    }
                    return new RefreshResult(task.getAdminAddress(), false);
                });
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(List<RefreshResult> results) {
        int successCount = 0;
        int failureCount = 0;
        for (var result : results) {
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(result.getAddress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }

    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }
}