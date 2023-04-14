package course.concurrency.m3_shared.immutable;

import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.IN_PROGRESS;
import static course.concurrency.m3_shared.immutable.Order.Status.NEW;

public final class Order {

    public enum Status {NEW, IN_PROGRESS, DELIVERED;}

    private final Long id;

    private final List<Item> items;

    private final PaymentInfo paymentInfo;

    private final boolean isPacked;

    private final Status status;

    public Order(Long id, List<Item> items, boolean isPacked, PaymentInfo paymentInfo, Status status) {
        this.id = id;
        this.items = items;
        this.isPacked = isPacked;
        this.paymentInfo = paymentInfo;
        this.status = status;
    }

    public Order(Long id, List<Item> items) {
        this(id, items, false, null, NEW);
    }

    public Order withPacked(boolean isPacked) {
        return new Order(this.id, this.items, isPacked, paymentInfo, IN_PROGRESS);
    }

    public Order withPaymentInfo(PaymentInfo paymentInfo) {
        return new Order(this.id, this.items, this.isPacked, paymentInfo, IN_PROGRESS);
    }

    public Order withStatus(Status status) {
        return new Order(id, items, isPacked, paymentInfo, status);
    }

    public boolean checkStatus() {
        return items != null && !items.isEmpty() && paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return items;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Status getStatus() {
        return status;
    }
}
