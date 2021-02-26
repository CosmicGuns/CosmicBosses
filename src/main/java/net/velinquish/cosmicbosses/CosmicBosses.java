package net.velinquish.cosmicbosses;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.drops.DropManager;
import lombok.Getter;
import net.velinquish.cosmicbosses.commands.CommandManager;
import net.velinquish.utils.Common;
import net.velinquish.utils.VelinquishPlugin;
import net.velinquish.utils.lang.LangManager;

public class CosmicBosses extends JavaPlugin implements Listener, VelinquishPlugin {

	@Getter
	private static CosmicBosses instance;
	@Getter
	private LangManager langManager;

	@Getter
	private String prefix;
	@Getter
	private String permission;

	private static boolean debug;

	@Getter
	private YamlConfiguration config;
	private File configFile;

	private YamlConfiguration lang;
	private File langFile;

	private YamlConfiguration kitsConfig;
	private File kitsFile;

	private Map<String, List<ItemStack>> kits;

	private Map<UUID, String> instantDrops; //String represents the name of the kit to drop
	private Map<UUID, String> drops; //Same as above except normal death drop
	private Map<UUID, List<Player>> lootList;

	@Override
	public void onEnable() {
		instance = this;
		Common.setInstance(this);

		langManager = new LangManager();

		kits = new HashMap<>();
		instantDrops = new HashMap<>();
		drops = new HashMap<>();
		lootList = new LinkedHashMap<>();

		try {
			loadFiles();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}


		getServer().getPluginManager().registerEvents(this, this);
		Common.registerCommand(new CommandManager(config.getString("main-command")));
	}

	@Override
	public void onDisable() {
		for (UUID id : lootList.keySet()) {
			Entity boss = getEntity(id);
			if (boss.isValid()) {
				MythicMobs.inst().getMobManager().getMythicMobInstance(boss).setDespawned();
				boss.remove();
			}
		}
		instance = null;
	}

	public void loadFiles() throws IOException, InvalidConfigurationException {
		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("config.yml", false);
		}
		config = new YamlConfiguration();
		config.load(configFile);

		prefix = getConfig().getString("plugin-prefix");
		debug = getConfig().getBoolean("debug");
		permission = getConfig().getString("permission");

		langFile = new File(getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			langFile.getParentFile().mkdirs();
			saveResource("lang.yml", false);
		}
		lang = new YamlConfiguration();
		lang.load(langFile);

		langManager.clear();
		langManager.setPrefix(prefix);
		langManager.loadLang(lang);

		kitsFile = new File(getDataFolder(), "kits.yml");
		if (!kitsFile.exists()) {
			kitsFile.getParentFile().mkdirs();
			saveResource("kits.yml", false);
		}
		kitsConfig = new YamlConfiguration();
		kitsConfig.load(kitsFile);

		loadKits();
	}

	public String defaultString(String loc, String path) {
		String string = config.getString(loc + path);
		if (string == null)
			return getConfig().getString("defaults." + path);
		return string;
	}

	public int defaultInt(String loc, String path) {
		int setting = config.getInt(loc + path);
		if (setting == 0)
			return getConfig().getInt("defaults." + path);
		return setting;
	}

	/**
	 * Gets all the names of all the kits loaded
	 * @return all the names of all the kits loaded
	 */
	public Set<String> getKits() {
		return kits.keySet();
	}

	/**
	 * Gets the items in the specified kit
	 * @param kit name of the kit
	 * @return the items in the kit or null if it does not exist
	 */
	public List<ItemStack> getKit(String kit) {
		return kits.get(kit);
	}

	@SuppressWarnings("unchecked")
	private void loadKits() {
		kits.clear();
		if (kitsConfig.getConfigurationSection("kits") == null) //no kits
			return;
		for (String kit : kitsConfig.getConfigurationSection("kits").getKeys(false))
			kits.put(kit, (List<ItemStack>) kitsConfig.getList("kits." + kit));
	}


	/**
	 * Adds an item to the kit
	 * @param kit name of the kit
	 * @param toSave item to save to the kit
	 * @return true if the item's been added to an existing kit or false if a new kit was created
	 */
	public boolean addToKit(String kit, ItemStack toSave) {
		List<ItemStack> list = kits.get(kit);
		if (list == null) {
			list = new ArrayList<>();
			list.add(toSave);
			createKit(kit, list);
			return false;
		}
		list.add(toSave);
		setKit(kit, list, false);
		return true;
	}

	/**
	 * Sets a list of itemstacks as a kit
	 * @param kit name of the kit
	 * @param toSave item list to save to the kit
	 * @param checkNull whether to check if the kit is null
	 * @return
	 */
	public boolean setKit(String kit, List<ItemStack> toSave, boolean checkNull) {
		if (checkNull && getKit(kit) == null) {
			createKit(kit, toSave);
			return false;
		}
		kits.replace(kit, toSave);
		kitsConfig.set("kits." + kit, toSave);
		try {
			kitsConfig.save(kitsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void createKit(String kit, List<ItemStack> toSave) {
		YamlConfiguration.createPath(kitsConfig, "kits." + kit);
		kitsConfig.set("kits." + kit, toSave);
		try {
			kitsConfig.save(kitsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		kits.put(kit, toSave);
	}

	public void checkMaxBosses() {
		if (lootList.size() >= config.getInt("max-bosses")) {
			UUID id = lootList.entrySet().iterator().next().getKey();
			drops.remove(id);
			instantDrops.remove(id);
			lootList.remove(id);
			debug("The boss number will exceed the max-boss limit! Despawning one.");
			Entity entity = getEntity(id);
			if (entity != null && entity.isValid()) {
				MythicMobs.inst().getMobManager().getMythicMobInstance(entity).setDespawned();
				entity.remove();
			}
		}
	}

	public void registerBoss(UUID entity, String id, String instantKit, String dropKit) {
		instantDrops.put(entity, instantKit);
		drops.put(entity, dropKit);
		lootList.put(entity, new ArrayList<>());

		// Despawning should now be done through MythicMobs skills mechanic
		//int despawnTime = defaultInt("bosses." + id + ".", "despawn-time");
		//if (despawnTime < 0)
		//	return;

		//Bukkit.getScheduler().runTaskLater(this, () -> {
		//	if (entity.isValid()) { //Reason for space_withers failing to despawn: entity isn't valid or loaded in anymore when a player isn't near it
		//		//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm m kill " + id);
		//		debug("Despawning a boss due to the time being reached.");
		//		MythicMobs.inst().getMobManager().getMythicMobInstance(entity).unregister();
		//		entity.remove();
		//		drops.remove(entity);
		//		instantDrops.remove(entity);
		//		lootList.remove(entity);
		//		String perm = defaultString("bosses." + id + ".", "permission");
		//		if (perm == null)
		//			Bukkit.broadcastMessage(Common.colorize(defaultString("bosses." + id + ".", "broadcast")));
		//		else
		//			Bukkit.broadcast(Common.colorize(defaultString("bosses." + id + ".", "broadcast")), perm);
		//	}
		//}, despawnTime * 20);
	}

	@EventHandler
	public void onBossDamage(EntityDamageByEntityEvent e) {
		UUID id = e.getEntity().getUniqueId();
		if (!instantDrops.containsKey(id) || !(e.getDamager() instanceof Player) || lootList.get(id).contains(e.getDamager()))
			return;

		lootList.get(id).add((Player) e.getDamager());
		langManager.getNode("joined-in-boss-fight").execute(e.getDamager());
	}

	@EventHandler
	public void onBossCrackShotDamage(WeaponDamageEntityEvent e) {
		UUID id = e.getVictim().getUniqueId();
		if (!instantDrops.containsKey(id) || lootList.get(id).contains(e.getPlayer()))
			return;

		lootList.get(id).add(e.getPlayer());
		langManager.getNode("joined-in-boss-fight").execute(e.getPlayer());
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onBossDeath(MythicMobDeathEvent e) {
		UUID entity = e.getEntity().getUniqueId();

		if (!lootList.containsKey(entity))
			return;

		debug("Assigned drop id is null? " + (drops.get(entity) == null));
		if (drops.get(entity) != null) {
			MythicMobs.inst().getDropManager();
			DropManager.Drop(e.getEntity().getLocation(), 0, kits.get(drops.get(entity)));
		}

		for (Player player : lootList.get(entity)) {
			for (ItemStack item : kits.get(instantDrops.get(entity)))
				player.getInventory().addItem(item);
			langManager.getNode("instant-drop-message").execute(player);
		}
		for (Player player : Bukkit.getOnlinePlayers())
			langManager.getNode("boss-death-broadcast").execute(player);
		instantDrops.remove(entity);
		drops.remove(entity);
		lootList.remove(entity);
	}

	private Entity getEntity(UUID id) {
		for (World world : Bukkit.getWorlds())
			for (Entity ent : world.getEntities())
				if (ent.getUniqueId().equals(id))
					return ent;
		return null;
	}

	public static void debug(String message) {
		if (debug == true)
			Common.log(message);
	}
}
