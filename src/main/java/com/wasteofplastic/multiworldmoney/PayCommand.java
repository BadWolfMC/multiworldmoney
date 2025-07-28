package com.wasteofplastic.multiworldmoney;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand implements CommandExecutor {
    private final MultiWorldMoney plugin;

    public PayCommand(MultiWorldMoney plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        Player payer = (Player) sender;
        Player payee = Bukkit.getPlayer(args[0]);
        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            payer.sendMessage(ChatColor.RED + "Invalid amount: " + args[1]);
            return true;
        }

        // Round to 2 decimal places
        amount = plugin.roundToTwoDecimals(amount);

        if (amount <= 0) {
            payer.sendMessage(ChatColor.RED + "Amount must be positive.");
            return true;
        }

        if (payee == null || !payee.isOnline()) {
            payer.sendMessage(ChatColor.RED + "Player not found or not online: " + args[0]);
            return true;
        }

        UUID payerUUID = payer.getUniqueId();
        UUID payeeUUID = payee.getUniqueId();
        World payerWorld = payer.getWorld();
        World payeeWorld = payee.getWorld();

        // Ensure both players are in the same group
        String payerGroup = plugin.getSettings().getWorldGroup(payerWorld);
        String payeeGroup = plugin.getSettings().getWorldGroup(payeeWorld);

        if (payerGroup == null || payeeGroup == null || !payerGroup.equalsIgnoreCase(payeeGroup)) {
            payer.sendMessage(ChatColor.RED + "You can only pay players in your current world group.");
            return true;
        }

        double balance = plugin.getVh().getBalance(payerUUID, payerWorld);
        if (balance < amount) {
            payer.sendMessage(ChatColor.RED + "You don't have enough money to send that amount.");
            return true;
        }

        plugin.getVh().withdraw(payerUUID, payerWorld, amount);
        plugin.getVh().deposit(payeeUUID, payeeWorld, amount);

        payer.sendMessage(ChatColor.GREEN + "You paid $" + amount + " to " + payee.getName());
        payee.sendMessage(ChatColor.GREEN + "You received $" + amount + " from " + payer.getName());

        return true;
    }
}
