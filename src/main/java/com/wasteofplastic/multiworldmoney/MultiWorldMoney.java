package com.wasteofplastic.multiworldmoney;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.core.MultiverseCoreApi;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

public class MultiWorldMoney extends JavaPlugin {

    private final HashMap<String, List<World>> worldGroups = new HashMap<>();
    private final HashMap<World, String> reverseWorldGroups = new HashMap<>();
    private PlayerCache players;
    private VaultHelper vh;
    private Settings settings;

    @Override
    public void onEnable() {
        // Load Settings
        settings = new Settings();

        // Load cache
        players = new PlayerCache(this);

        // Load Vault
        vh = new VaultHelper(this);

        // Load world groups
        GroupLoader.loadGroups(this);

        // Register listener
        getServer().getPluginManager().registerEvents(new WorldChangeListener(this), this);

        // Register commands
        PluginCommand mvmCommand = this.getCommand("mvm");
        if (mvmCommand != null) {
            AdminCommands adminCommands = new AdminCommands(this);
            mvmCommand.setExecutor(adminCommands);
        }

        // Log Multiverse-Core availability
        Plugin mvPlugin = this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (mvPlugin != null && mvPlugin.isEnabled()) {
            getLogger().info("Multiverse-Core found.");
        } else {
            getLogger().info("Multiverse-Core not found.");
        }

        // Check if this is an upgrade
        File userDataFolder = new File(getDataFolder(),"userdata");
        if (userDataFolder.exists()) {
            getLogger().info("Upgrading old database to UUID's. This may take some time.");
            File playersFolder = new File(getDataFolder(), "players");
            if (!playersFolder.exists()) {
                playersFolder.mkdir();
            }
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        players.saveAll();
    }

    public PlayerCache getPlayers() {
        return players;
    }

    public VaultHelper getVh() {
        return vh;
    }

    public Settings getSettings() {
        return settings;
    }

    public HashMap<String, List<World>> getWorldGroups() {
        return worldGroups;
    }

    public HashMap<World, String> getReverseWorldGroups() {
        return reverseWorldGroups;
    }

    public double roundToTwoDecimals(double amount) {
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
} 
