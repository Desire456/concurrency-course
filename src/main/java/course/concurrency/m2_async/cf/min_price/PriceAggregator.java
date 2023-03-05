package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceAggregator {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    private PriceRetriever priceRetriever = new PriceRetriever();

    private Collection<Long> shopIds = Set.of(10L, 45L, 66L, 345L, 234L, 333L, 67L, 123L, 768L);

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        var retrievePriceJobs = shopIds.stream()
                .map(shopId -> retrievePrice(itemId, shopId))
                .collect(Collectors.toList());
        return CompletableFuture.allOf(retrievePriceJobs.toArray(new CompletableFuture[0]))
                .thenApply(unused -> retrievePriceJobs.stream()
                        .mapToDouble(CompletableFuture::join)
                        .filter(price -> !Double.isNaN(price))
                        .min())
                .join()
                .orElse(Double.NaN);
    }

    private CompletableFuture<Double> retrievePrice(Long itemId, Long shopId) {
        return CompletableFuture.supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                .exceptionally(t -> Double.NaN)
                .completeOnTimeout(Double.NaN, 2950, TimeUnit.MILLISECONDS);
    }
}
