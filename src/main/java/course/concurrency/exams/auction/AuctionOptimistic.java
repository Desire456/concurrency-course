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
        Bid prev;
        do {
            prev = latestBid.get();
            if (bid.getPrice() <= prev.getPrice()) {
                break;
            }
        } while (!latestBid.compareAndSet(prev, bid));

        if (bid == latestBid.get()) {
            notifier.sendOutdatedMessage(prev);
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
