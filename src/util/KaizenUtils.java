package util;
import java.util.*;
import java.util.function.*;
public class KaizenUtils {
    public static <T> List<T> filter(List<T> list, Predicate<T> pred) {
        List<T> r = new ArrayList<>();
        for (T item : list) if (pred.test(item)) r.add(item);
        return r;
    }
    public static <T, K> Map<K, List<T>> groupBy(List<T> list, Function<T,K> fn) {
        Map<K, List<T>> map = new LinkedHashMap<>();
        for (T item : list) map.computeIfAbsent(fn.apply(item), k -> new ArrayList<>()).add(item);
        return map;
    }
    public static <T, K extends Comparable<K>> List<T> sortBy(
            List<T> list, Function<T,K> fn, boolean desc) {
        List<T> sorted = new ArrayList<>(list);
        sorted.sort((a,b) -> { int c = fn.apply(a).compareTo(fn.apply(b)); return desc ? -c : c; });
        return sorted;
    }
    public static <T> Optional<T> findFirst(List<T> list, Predicate<T> pred) {
        for (T item : list) if (pred.test(item)) return Optional.of(item);
        return Optional.empty();
    }
    public static <T> Page<T> paginate(List<T> all, int page, int size) {
        int from = page * size, to = Math.min(from + size, all.size());
        if (from >= all.size()) return new Page<>(Collections.emptyList(), page, size, all.size());
        return new Page<>(all.subList(from, to), page, size, all.size());
    }
    public static <T> Optional<T> safeCast(Object obj, Class<T> type) {
        if (type.isInstance(obj)) return Optional.of(type.cast(obj));
        return Optional.empty();
    }
}
