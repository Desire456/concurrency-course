package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
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

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
