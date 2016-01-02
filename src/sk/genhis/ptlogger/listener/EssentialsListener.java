package sk.genhis.ptlogger.listener;

import net.ess3.api.events.AfkStatusChangeEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import sk.genhis.ptlogger.PTLogger;

public final class EssentialsListener implements Listener {
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onAfkStatusChange(AfkStatusChangeEvent e) {
		String p = e.getController().getName();
		if(!e.getValue())
			PTLogger.playerJoined(p);
		else
			PTLogger.playerLeft(p);
	}
}