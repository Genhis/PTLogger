package sk.genhis.ptlogger.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import sk.genhis.ptlogger.PTLogger;

public final class BukkitListener implements Listener {
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(!PTLogger.isPaused())
			PTLogger.playerJoined(e.getPlayer().getName());
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(!PTLogger.isPaused())
			PTLogger.playerLeft(e.getPlayer().getName());
	}
}