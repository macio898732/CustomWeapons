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
import org.bukkit.inventory.meta.SkullMeta;
import java.util.Collections;

public class BalloonCommand implements CommandExecutor {
    private static final String BALLOON_NAME = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Balloon";
    private static final String BALLOON_TEXTURE = "b03338e1e9ae77cb28a195790fcbc0601c6588830ca429af19205c3e0642bed7";
    private static final int CUSTOM_MODEL_DATA = 102;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(ChatColor.RED + "You must be holding a player head to turn it into a Balloon!");
            return true;
        }
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setDisplayName(BALLOON_NAME);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right or left click to launch a balloon!"));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        item.setItemMeta(meta);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Your head has been turned into a Balloon!");
        return true;
    }

    private ItemStack getCustomHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(BALLOON_NAME);
        meta.setOwningPlayer(Bukkit.getOfflinePlayer("Wutt"));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        head.setItemMeta(meta);
        return head;
    }
} 