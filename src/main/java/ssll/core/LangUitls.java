package ssll.core;

import java.util.concurrent.ConcurrentMap;

public class LangUitls {

	public static <E> E getOrElse(E nullable, E other) {
		return nullable == null ? other : nullable;
	}

	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
		V v = map.putIfAbsent(key, value);
		return v == null ? value : v;
	}

}
