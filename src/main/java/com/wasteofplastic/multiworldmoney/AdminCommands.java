package com.wasteofplastic.multiworldmoney;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.MultiverseWorld;

import java.util.UUID;

public class AdminCommands implements CommandExecutor {
    private final MultiWorldMoney plugin;

    public AdminCommands(MultiWorldMoney plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mwm.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /mvm <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        double amount;

        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
            return true;
        }

        // Round amount
        amount = plugin.roundToTwoDecimals(amount);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        UUID uuid = target.getUniqueId();
        World world = target.getWorld();

        switch (action) {
            case "give":
                plugin.getVh().deposit(uuid, world, amount);
                sender.sendMessage(ChatColor.GREEN + "Gave $" + amount + " to " + target.getName());
                break;
            case "take":
                double currentBalance = plugin.getVh().getBalance(uuid, world);
                if (currentBalance < amount) {
                    sender.sendMessage(ChatColor.RED + target.getName() + " does not have enough money.");
                    return true;
                }
                plugin.getVh().withdraw(uuid, world, amount);
                sender.sendMessage(ChatColor.GREEN + "Took $" + amount + " from " + target.getName());
                break;
            case "set":
                plugin.getVh().set(uuid, world, amount);
                sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to $" + amount);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                break;
        }

        return true;
    }
}
