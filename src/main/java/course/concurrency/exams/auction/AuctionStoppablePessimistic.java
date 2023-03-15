package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private final Notifier notifier;
    private final StoppableLock stoppableLock = new StoppableLock();

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid;

    public boolean propose(Bid bid) {
        synchronized (stoppableLock) {
            var stopped = checkStop();
            if (!stopped && (latestBid == null || bid.getPrice() > latestBid.getPrice())) {
                notifier.sendOutdatedMessage(latestBid);
                latestBid = bid;
                return true;
            }
            return false;
        }
    }

    private boolean checkStop() {
        if (stoppableLock.stop) {
            try {
                stoppableLock.wait(3000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    public Bid getLatestBid() {
        synchronized (stoppableLock) {
            return latestBid;
        }
    }

    public Bid stopAuction() {
        synchronized (stoppableLock) {
            stoppableLock.stop = true;
            return latestBid;
        }
    }

    static class StoppableLock {
        private boolean stop = false;
    }
}
