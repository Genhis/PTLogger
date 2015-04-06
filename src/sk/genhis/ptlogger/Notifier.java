package sk.genhis.ptlogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Notifier {
	protected final long time;
	protected final String player;
	
	private static final Map<String, List<Notifier>> listeners = new HashMap<String, List<Notifier>>();
	
	public Notifier(String player, long time) {
		this.player = player;
		this.time = PTLogger.getPlayerTime(player) + time;
		
		if(!Notifier.listeners.containsKey(player))
			Notifier.listeners.put(player, new ArrayList<Notifier>());
		if(!Notifier.listeners.get(player).contains(this))
			Notifier.listeners.get(player).add(this);
	}
	
	public abstract void onCheck();
	public abstract void onFinish();
	
	public long timeLeft() {
		return this.time - PTLogger.getPlayerTime(this.player);
	}
	
	public static void checkPlayer(String player) {
		final List<Notifier> del = new ArrayList<Notifier>();
		final long time = PTLogger.getPlayerTime(player);
		for(Notifier n : Notifier.listeners.get(player)) {
			n.onCheck();
			if(time >= n.time) {
				n.onFinish();
				del.add(n);
			}
		}
		Notifier.listeners.get(player).removeAll(del);
	}
}