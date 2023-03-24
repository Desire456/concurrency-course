package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>(
            new Bid(-1L, -1L, -1L)
    );
    private volatile boolean stop = false;

    public boolean propose(Bid bid) {
        Bid prev;
        do {
            prev = latestBid.get();
            if (stop || bid.getPrice() <= prev.getPrice()) {
                return false;
            }
        } while (!stop && !latestBid.compareAndSet(prev, bid));

        if (stop) {
            return false;
        }

        notifier.sendOutdatedMessage(prev);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }

    public Bid stopAuction() {
        stop = true;
        return latestBid.get();
    }
}
