package ssll.rsm.fn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import ssll.core.SilentCloseable;

public class LocalData implements SilentCloseable {

	private final ThreadLocal<Deque<LocalData>> local;
	final Map<String, Map> map = new HashMap<>();

	private LocalData(ThreadLocal<Deque<LocalData>> local) {
		this.local = local;
	}

	public static LocalData build(ThreadLocal<Deque<LocalData>> local) {
		LocalData t = new LocalData(local);
		Deque<LocalData> q = local.get();
		if (q == null) {
			q = new ArrayDeque<>();
			local.set(q);
		}
		q.addLast(t);
		return t;
	}

	@Override
	public void close() {
		Deque<LocalData> q = local.get();
		if (q == null || !q.contains(this)) {
			return;
		}
		while (!q.isEmpty()) {
			LocalData e = q.removeLast();
			if (e == this) {
				break;
			}
		}
		if (q.isEmpty()) {
			local.remove();
		}
	}

}
