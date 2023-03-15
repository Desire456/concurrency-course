package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>();
    private volatile boolean stop = false;

    public boolean propose(Bid bid) {
        if (!stop) {
            var prev = latestBid.get();
            if (prev == null) {
                latestBid.set(bid);
                prev = latestBid.get();
            }

            if (bid.getPrice() > prev.getPrice() && latestBid.compareAndSet(prev, bid)) {
                notifier.sendOutdatedMessage(prev);
                return true;
            }

            return false;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    public Bid stopAuction() {
        stop = true;
        return latestBid.get();
    }
}
