package course.concurrency.m3_shared.immutable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static course.concurrency.m3_shared.immutable.Order.Status.DELIVERED;

public class OrderService {

    private Map<Long, Order> currentOrders = new HashMap<>();
    private long nextId = 0L;

    private synchronized long nextId() {
        return nextId++;
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        Order order = new Order(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public synchronized void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        var order = currentOrders.get(orderId).withPaymentInfo(paymentInfo);
        if (order.checkStatus()) {
            deliver(order);
        } else {
            currentOrders.put(orderId, order);
        }
    }

    public synchronized boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).getStatus().equals(DELIVERED);
    }

    public synchronized void setPacked(long orderId) {
        var order = currentOrders.get(orderId).withPacked(true);
        if (order.checkStatus()) {
            deliver(order);
        } else {
            currentOrders.put(orderId, order);
        }
    }

    private void deliver(Order order) {
        /* ... */
        currentOrders.put(order.getId(), order.withStatus(DELIVERED));
    }

}
