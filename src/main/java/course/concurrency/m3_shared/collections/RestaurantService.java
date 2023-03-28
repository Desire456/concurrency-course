package course.concurrency.m3_shared.collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class RestaurantService {

    private final Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>(
            Map.of("A", new Restaurant("A"),
                    "B", new Restaurant("B"),
                    "C", new Restaurant("C"))
    );

    private final Map<String, LongAdder> statistics = new ConcurrentHashMap<>();

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        statistics.computeIfAbsent(restaurantName, (k) -> new LongAdder()).increment();
    }

    public Set<String> printStat() {
        return statistics.entrySet().stream()
                .map(e -> e.getKey().concat(" - ").concat(e.getValue().toString()))
                .collect(Collectors.toSet());
    }
}
