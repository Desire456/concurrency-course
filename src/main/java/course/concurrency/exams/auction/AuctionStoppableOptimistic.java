package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private final AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference<>(
            new Bid(-1L, -1L, -1L),
            false
    );

    private final Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        Bid prev;
        do {
            prev = latestBid.getReference();
            if (latestBid.isMarked() || bid.getPrice() <= prev.getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(prev, bid, false, false));

        notifier.sendOutdatedMessage(prev);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        latestBid.set(getLatestBid(), true);
        return getLatestBid();
    }
}
