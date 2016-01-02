package sk.genhis.ptlogger.listener;

import net.ess3.api.events.AfkStatusChangeEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;

import sk.genhis.ptlogger.PTLogger;

public final class VanishNoPacketEssentialsListener implements Listener {
	private VanishManager vanish = ((VanishPlugin)Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager();
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onAfkStatusChangeLowest(AfkStatusChangeEvent e) {
		String p = e.getController().getName();
		if(e.getValue() && this.vanish.isVanished(p))
			PTLogger.playerUnvanished(p);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onAfkStatusChangeMonitor(AfkStatusChangeEvent e) {
		String p = e.getController().getName();
		if(!e.getValue() && this.vanish.isVanished(p))
			PTLogger.playerVanished(p);
	}
}