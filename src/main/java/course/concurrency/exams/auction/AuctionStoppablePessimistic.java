package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final Object lock = new Object();
    private volatile boolean stop = false;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(-1L, -1L, -1L);

    public boolean propose(Bid bid) {
        if (!stop && bid.getPrice() > latestBid.getPrice()) {
            synchronized (lock) {
                if (!stop && bid.getPrice() > latestBid.getPrice()) {
                    notifier.sendOutdatedMessage(latestBid);
                    latestBid = bid;
                    return true;
                }
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        stop = true;
        return latestBid;
    }
}
