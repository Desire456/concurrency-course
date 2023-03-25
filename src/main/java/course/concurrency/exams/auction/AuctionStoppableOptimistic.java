package course.concurrency.exams.auction;

import org.apache.commons.math3.util.Pair;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    //    private final AtomicReference<Bid> latestBid = new AtomicReference<>(
//            new Bid(-1L, -1L, -1L)
//    );
    private final AtomicReference<Pair<Bid, Boolean>> latestBid = new AtomicReference<>(
            new Pair<>(
                    new Bid(-1L, -1L, -1L),
                    false
            )
    );
    private final Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    public boolean propose(Bid bid) {
        var newBid = new Pair<>(bid, false);
        Pair<Bid, Boolean> prev;
        do {
            prev = latestBid.get();
            if (prev.getValue() || newBid.getKey().getPrice() <= prev.getKey().getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(prev, newBid));

        notifier.sendOutdatedMessage(prev.getKey());
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get().getKey();
    }

    public Bid stopAuction() {
        latestBid.set(new Pair<>(getLatestBid(), true));
        return getLatestBid();
    }

//    public boolean propose(Bid bid) {
//        Bid prev;
//        do {
//            prev = latestBid.get();
//            if (stop || bid.getPrice() <= prev.getPrice()) {
//                return false;
//            }
//        } while (!stop && !latestBid.compareAndSet(prev, bid));
//
//        if (stop) {
//            return false;
//        }
//
//        notifier.sendOutdatedMessage(prev);
//        return true;
//    }
//
//    public Bid getLatestBid() {
//        return latestBid.get();
//    }
//
//    public Bid stopAuction() {
//        stop = true;
//        return latestBid.get();
//    }
}
