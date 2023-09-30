package net.qfive.nightvoter;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NightVoter extends JavaPlugin {
	
	public static JavaPlugin plugin;

    // Setup vars
    static Map<String, Integer> numberOfVotes = new HashMap<String, Integer>();
    static List<String> currentlyNight = new ArrayList<String>();
    static Multimap<String, String> userVotes = ArrayListMultimap.create();
    static List<String> warnedWorlds = new ArrayList<String>();

    /*
    TODO:
    - Update checking
    - Admin commands
    - Ability to alter percentage of players that triggers vote
    - Ability to change reset time, night time, etc. (also add vote for day, keep night, etc)
    */



    // Get config
    private YamlDocument config;

    @SuppressWarnings("deprecation")
	@Override
    public void onEnable() {
        // When server enables plugin

    	plugin = this;

        try {
            config = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        addWorlds();


        try {
            config.update();
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', NightVoter.plugin.getConfig().getString("message_prefix") + " ");
    	
        // Register commands
        this.getCommand("night").setExecutor(new CommandNight());
        this.getCommand("nightadmin").setExecutor(new CommandNightAdmin());

        // Say the plugin is enabled
        getLogger().info("Enabled NightVoter");

        // Start scheduler for checking world time
        BukkitScheduler timescheduler = getServer().getScheduler();
        timescheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (World w : Bukkit.getServer().getWorlds()) {
                    if (config.getBoolean("worlds." + w.getName())) {
                        if (!(currentlyNight.contains(w.getName()))) {
                            if ((w.getTime() > 12960L && w.getTime() < 23041L) && getServer().getOnlinePlayers().size() >= 1) {
                                if (processVotes(w.getName(), getServer().getOnlinePlayers().size())) {
                                    // If 25% of players ...
                                    currentlyNight.add(w.getName());
                                    getServer().broadcastMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + "25% or more of players have voted to not skip night in " + w.getName() + "!");
                                } else {
                                    if (warnedWorlds.contains(w.getName())) {
                                    	warnedWorlds.remove(w.getName());
                                    }
                                    w.setTime(0);
                                }
                            } else if (
                            		getServer().getOnlinePlayers().size() >= 1 &&
                            		!(warnedWorlds.contains(w.getName())) &&
                            		w.getTime() < 12960L &&
                            		w.getTime() > 12160L &&
                            		config.getBoolean("warning_enabled")
                            		) {
                            	// Warn players vote will end soon
                            	getServer().broadcastMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + "The time left to vote will end soon in " + w.getName() + "!");
                            	warnedWorlds.add(w.getName());
                            }
                        }
                    }
                    if (currentlyNight.contains(w.getName())) {
                        if (w.getTime() > 23041L || (w.getTime() >= 7L && w.getTime() < 12960L)) {
                            currentlyNight.remove(w.getName());
                            numberOfVotes.put(w.getName(), 0);
                            userVotes.removeAll(w.getName());
                            if (warnedWorlds.contains(w.getName())) {
                            	warnedWorlds.remove(w.getName());
                            }
                            getServer().broadcastMessage(ChatColor.GOLD + prefix + ChatColor.WHITE + "Night votes reset for " + w.getName() + "!");
                        }
                    }
                }
            }
        }, 0L, (config.getLong("time_check_interval")) * 20);

        // Start scheduler for broadcast message
        BukkitScheduler messagescheduler = getServer().getScheduler();
        messagescheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (getServer().getOnlinePlayers().size() >= 1) {
                    getServer().broadcastMessage(prefix + ChatColor.WHITE + "Don't want to skip night? Use " + ChatColor.AQUA + "/night " + ChatColor.WHITE + "to vote.");
                }
            }
        }, 20L, (config.getLong("message_interval") * 20));
        
        // Don't think this will work
        /*
        int counter = 0;
        // Start scheduler for voting time alert
        BukkitScheduler warningscheduler = getServer().getScheduler();
        warningscheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
            	for (i = 0; i < worldList.length; i++) {
            		counter++;
            		World loadedWorld = Bukkit.getServer().getWorld(worldList.get(counter));
	                if (getServer().getOnlinePlayers().size() >= 1 && ()) {
	                    getServer().broadcastMessage(ChatColor.GOLD + prefix + ChatColor.AQUA + config.getLong("warning_time") + " seconds " + ChatColor.WHITE + "left to vote for night in " + worldname);
	                }
                }
            }
        }, 20L, (config.getLong("time_check_interval") * 20));
*/

    }

    @Override
    public void onDisable() {
        // When server disables plugin

        // Save config?
        getLogger().info("Disabled NightVoter");
    }


    public static int addVote(Player player, String prefix, boolean displayWorldNameEnabled) {

        World world = player.getWorld();
        String worldname = player.getWorld().getName();
        String username = player.getName();

        if (numberOfVotes.containsKey(worldname)) { // Check if world is in enabled NightVoter worlds
			if (!(userVotes.containsEntry(worldname, username))) {
                if (player.getWorld().getTime() >= 8L && (player.getWorld().getTime() < 12960L || player.getWorld().getTime() > 23041L)) {

                    int temp = numberOfVotes.get(worldname);
                    temp++;
                    numberOfVotes.put(worldname, temp); // Add number of votes

                    userVotes.put(worldname, username); // Add user has voted for this world

                    player.sendMessage(prefix + ChatColor.WHITE + "You have voted to not skip night in " + getWorldName(world, displayWorldNameEnabled) + "!");
                    return 1; // World is allowed, vote registered
                } else {
                    // World is night, cannot vote
                    player.sendMessage(prefix + ChatColor.WHITE + "You cannot vote while it is night.");
                    return 3;
                }
            } else {
                // Player has already voted for world
                player.sendMessage(prefix + ChatColor.WHITE + "You have already voted for night in " + getWorldName(world, displayWorldNameEnabled) + ".");
                return 4;
            }
        } else {
            // World is not allowed
        	player.sendMessage(prefix + ChatColor.WHITE + "You cannot vote for night in " + getWorldName(world, displayWorldNameEnabled) + "!");
            return 2;
        }
    }

    static boolean processVotes(String world, int playerCount) {
        return numberOfVotes.get(world) >= (playerCount * 0.25); // True == allow night
    }
    
    static String getWorldName(World world, boolean enabled) {
    	
    	if (enabled) { // If showing world name is enabled in config...
    		return world.getName(); // Return the world name
    	} else {
    		return "this world"; // Otherwise, return "this world"
    	}

    }

    public boolean reloadConf() {

        try {
            addWorlds();
            config.update();
            config.save();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



    void addWorlds() {
        boolean condition = true;

        // Add worlds to config if not already there
        for (World w : Bukkit.getServer().getWorlds()) {

            if (!config.contains("worlds." + w.getName())) {
                config.set("worlds." + w.getName(), false);

                // Add comment because it gets deleted for some reason if it's put in the config.yml in resources
                if (condition) {
                    config.addComment("""
                            # NightVoter
                            # Plugin by qfive
                            # qfive.net

                            # Worlds: true enables NightVoter for that world, false disables.
                            """);
                    condition = false;
                }
            }


        }

        for (World w : Bukkit.getServer().getWorlds()) {
            if (config.getBoolean("worlds." + w.getName())) {
                getLogger().info("Enabled world: " + w.getName());
                numberOfVotes.put(w.getName(), 0); // Put world name into vote list
            }
        }

    }


}




