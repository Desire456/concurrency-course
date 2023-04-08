package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("DataFlowIssue")
public class OrderService {

    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private long nextId = 0L;

    private synchronized long nextId() {
        return nextId++;
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        var order = new Order(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        var order = currentOrders.computeIfPresent(orderId, (k, v) -> v.withPaymentInfo(paymentInfo));
        deliverIfNecessary(order);
    }

    public void setPacked(long orderId) {
        var order = currentOrders.computeIfPresent(orderId, (k, v) -> v.withPacked(true));
        deliverIfNecessary(order);
    }

    private void deliverIfNecessary(Order order) {
        if (order.checkStatus()) {
            currentOrders.computeIfPresent(order.getId(), (k, v) -> v.withStatus(Order.Status.DELIVERED));
        }
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(Order.Status.DELIVERED);
    }
}
