package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private final Notifier notifier;
    private final Object lock = new Object();

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(-1L, -1L, -1L);

    public boolean propose(Bid bid) {
        boolean needUpdate;
        Bid outdated;
        synchronized (lock) {
            outdated = latestBid;
            needUpdate = bid.getPrice() > latestBid.getPrice();
            if (needUpdate) {
                latestBid = bid;
            }
        }
        if (needUpdate) {
            notifier.sendOutdatedMessage(outdated);
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
