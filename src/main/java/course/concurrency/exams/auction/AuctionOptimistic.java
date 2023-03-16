package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>(
            new Bid(-1L, -1L, -1L)
    );

    public boolean propose(Bid bid) {
        var prev = latestBid.get();
        if (bid == latestBid.updateAndGet(val -> bid.getPrice() > val.getPrice() ? bid : val)) {
            notifier.sendOutdatedMessage(prev);
            return true;
        }

        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
