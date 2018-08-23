package sk.genhis.ptlogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import sk.genhis.glib.GLib;
import sk.genhis.glib.LicenceChecker;
import sk.genhis.glib.Logger;
import sk.genhis.glib.configuration.Config;
import sk.genhis.glib.configuration.Configuration;
import sk.genhis.glib.configuration.ResourceReader;
import sk.genhis.glib.mysql.MySQL;
import sk.genhis.glib.plugin.GPlugin;
import sk.genhis.ptlogger.listener.*;

public final class PTLogger extends GPlugin {
	private static PTLogger plugin = null;
	private static Logger logger = null;
	private static Configuration config = null;
	private static MySQL mysql;
	
	private static Calendar cal = Calendar.getInstance();
	private static final Map<String, Long> stats = new HashMap<String, Long>();
	private static final Map<String, Long> vanish = new HashMap<String, Long>();
	private static final List<String> newp = new ArrayList<String>();
	
	private static boolean paused = false;
	
	private static UpdateDateTask udt = new UpdateDateTask();
	
	private boolean essentials = false;
	private boolean vanishNoPacket = false;
	
	static {
		PTLogger.cal.set(Calendar.HOUR_OF_DAY, 0);
		PTLogger.cal.set(Calendar.MINUTE, 0);
		PTLogger.cal.set(Calendar.SECOND, 0);
		PTLogger.cal.set(Calendar.MILLISECOND, 0);
	}
	
	@Override
	protected boolean enable() {
		PTLogger.plugin = this;
		PTLogger.logger = new Logger(this);
		PTLogger.config = new Configuration(new Config(this).getConfig());
		
		this.getOwnLogger().log("Kontrolujem licenciu");
		final LicenceChecker c = new LicenceChecker(this);
		if(!c.checkLicence()) {
			c.unlicenced();
			return false;
		}
		
		this.getOwnLogger().log("Hladam vlastnu konfiguraciu MySQL");
		if(this.getOwnConfig().getBoolean("mysql.enable", false)) {
			try {
				this.getOwnLogger().log("Nastavujem vlastnu konfiguraciu MySQL");
				PTLogger.mysql = new MySQL(this, this.getOwnConfig());
			}
			catch (SQLException ex) {
				this.getOwnLogger().log("Nastala chyba! Ponechavam povodnu konfiguraciu MySQL");
				PTLogger.mysql = GLib.getMysql();
				if(GLib.debug())
					ex.printStackTrace();
			}
		}
		else
			PTLogger.mysql = GLib.getMysql();

		this.getOwnLogger().log("Instalujem MySQL tabulky");
		try {
			this.getOwnMysql().connect();
			this.getOwnMysql().uquery(new ResourceReader(this, "tables.sql").getFileContent());
			this.getOwnMysql().disconnect();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		this.getOwnLogger().log("Hladam pluginy pre integraciu");
		if(this.essentials = Bukkit.getPluginManager().getPlugin("Essentials") != null)
			this.getOwnLogger().log("Zapinam integraciu s pluginom Essentials");
		if(this.vanishNoPacket = Bukkit.getPluginManager().getPlugin("VanishNoPacket") != null)
			this.getOwnLogger().log("Zapinam integraciu s pluginom VanishNoPacket");
		
		this.getOwnLogger().log("Prihlasujem pritomnych hracov");
		PTLogger.allJoin();
		
		this.getOwnLogger().log("Registrujem listenery");
		Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
		if(this.essentials)
			Bukkit.getPluginManager().registerEvents(new EssentialsListener(), this);
		if(this.vanishNoPacket)
			Bukkit.getPluginManager().registerEvents(new VanishNoPacketListener(), this);
		if(this.essentials && this.vanishNoPacket)
			Bukkit.getPluginManager().registerEvents(new VanishNoPacketEssentialsListener(), this);
		
		PTLogger.startUpdateTask();
		return true;
	}
	
	@Override
	protected void disable() {
		this.getOwnLogger().log("Odhlasujem pritomnych hracov a ukladam data");
		PTLogger.allLeave();
		
		this.getOwnLogger().log("Vypinam spustene ulohy");
		GLib.getScheduler().cancelTasks(this);
	}
	
	public static void loadPlayer(String player) {
		MySQL m = PTLogger.getPlugin().getOwnMysql();
		try {
			m.connect();
			ResultSet r = m.query("SELECT time, vanish FROM ptl_log WHERE username=? AND date=? LIMIT 1;", player, (PTLogger.cal.getTimeInMillis()/1000));
					//m.query("SELECT time, vanish FROM ptl_log WHERE username='" + player + "' AND date=" + (PTLogger.cal.getTimeInMillis() / 1000) + " LIMIT 1");
			if(r.next()) {
				PTLogger.stats.put(player, r.getLong("time"));
				PTLogger.vanish.put(player, r.getLong("vanish"));
			}
			else {
				PTLogger.stats.put(player, 0L);
				PTLogger.vanish.put(player, 0L);
				PTLogger.newp.add(player);
			}
			m.disconnect();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void savePlayer(String player) {
		if(PTLogger.getPlayerTime(player) < 30 && PTLogger.getPlayerVanishTime(player) == 0) { //ochrana proti botom, hráč musí byť online aspoň 30 sekúnd, aby sa uložil jeho čas
			PTLogger.stats.remove(player);
			PTLogger.vanish.remove(player);
			return;
		}
		
		MySQL m = PTLogger.getPlugin().getOwnMysql();
		try {
			m.connect();
			
			if(PTLogger.newp.contains(player)) {
				m.uquery("INSERT INTO ptl_log(username, date, time, vanish) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE time=?, vanish=?;", player, PTLogger.cal.getTimeInMillis()/1000, PTLogger.getPlayerTime(player),PTLogger.getPlayerVanishTime(player), PTLogger.getPlayerTime(player), PTLogger.getPlayerVanishTime(player));
				
				//m.uquery("INSERT INTO ptl_log(username, date, time, vanish) VALUES('" + player + "'," + (PTLogger.cal.getTimeInMillis() / 1000) + "," + PTLogger.getPlayerTime(player) + "," + PTLogger.getPlayerVanishTime(player) + ")");
				PTLogger.newp.remove(player);
			}
			else
				m.uquery("UPDATE ptl_log SET time = " + PTLogger.getPlayerTime(player) + ", vanish = " + PTLogger.getPlayerVanishTime(player) + " WHERE username='" + player + "' AND date=" + (PTLogger.cal.getTimeInMillis() / 1000));
			PTLogger.stats.remove(player);
			PTLogger.vanish.remove(player);
			
			m.disconnect();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void startUpdateTask() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		GLib.getScheduler().scheduleSyncRepeatingTask(PTLogger.getPlugin(), PTLogger.udt, (cal.getTimeInMillis() - System.currentTimeMillis()) * 20L, 86400 * 20L);
	}
	
	public static void playerJoined(String player) {
		PTLogger.loadPlayer(player);
		PTLogger.stats.put(player, PTLogger.stats.get(player).longValue() - System.currentTimeMillis() / 1000L);
	}
	
	public static void allJoin() {
		for(Player p : Bukkit.getOnlinePlayers())
			PTLogger.playerJoined(p.getName());
	}
	
	public static void playerLeft(String player) {
		PTLogger.stats.put(player, PTLogger.stats.get(player).longValue() + System.currentTimeMillis() / 1000L);
		PTLogger.savePlayer(player);
	}
	
	public static void allLeave() {
		for(Player p : Bukkit.getOnlinePlayers())
			PTLogger.playerLeft(p.getName());
	}
	
	public static void playerVanished(String player) {
		PTLogger.vanish.put(player, PTLogger.vanish.get(player).longValue() - System.currentTimeMillis() / 1000L);
	}
	
	public static void playerUnvanished(String player) {
		PTLogger.vanish.put(player, PTLogger.vanish.get(player).longValue() + System.currentTimeMillis() / 1000L);
	}
	
	public static void playerRelog(String player) {
		PTLogger.playerLeft(player);
		PTLogger.playerJoined(player);
	}
	
	public static void updateDate() {
		PTLogger.logger.log("Zacinam novy den");
		PTLogger.paused = true;
		PTLogger.allLeave();
		PTLogger.cal.add(Calendar.DAY_OF_MONTH, 1);
		PTLogger.paused = false;
		PTLogger.allJoin();
	}
	
	public static boolean isPaused() {
		return PTLogger.paused;
	}
	
	public static long getPlayerTime(String player) {
		return PTLogger.stats.get(player).longValue();
	}
	
	public static long getPlayerVanishTime(String player) {
		return PTLogger.vanish.get(player).longValue();
	}
	
	protected boolean enableMysql() {
		return true;
	}
	
	public static PTLogger getPlugin() {
		return PTLogger.plugin;
	}
	
	public Logger getOwnLogger() {
		return PTLogger.logger;
	}
	
	public Configuration getOwnConfig() {
		return PTLogger.config;
	}
	
	public MySQL getOwnMysql() {
		return PTLogger.mysql;
	}
}