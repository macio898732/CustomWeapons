package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Collections;

public class ScytheCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 126;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack scythe = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta meta = scythe.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Scythe");
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right-click a player to blind them for 10s (60s cooldown)"));
        scythe.setItemMeta(meta);
        player.getInventory().addItem(scythe);
        player.sendMessage(ChatColor.DARK_PURPLE + "You have received the Scythe!");
        return true;
    }
} 