package sk.genhis.ptlogger.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

import sk.genhis.ptlogger.PTLogger;

public final class VanishNoPacketListener implements Listener {
	private VanishManager vanish = ((VanishPlugin)Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager();
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onVanishStatusChange(VanishStatusChangeEvent e) {
		if(e.isVanishing())
			PTLogger.playerVanished(e.getName());
		else
			PTLogger.playerUnvanished(e.getName());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(this.vanish.isVanished(e.getPlayer()))
			PTLogger.playerUnvanished(e.getPlayer().getName());
	}
}
