package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class GetAwayCommand implements CommandExecutor {
    public static final String GETAWAY_NAME = ChatColor.translateAlternateColorCodes('&',
            "&d&lEscape Compass");
    public static final String GETAWAY_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Right-click to create decoys and teleport away");
    // Animation Made By Lusik21556/@Lusik21556
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check permission
        if (!player.hasPermission("getaway.give") && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(GETAWAY_NAME);
        meta.setLore(Arrays.asList(
                GETAWAY_LORE,
                ChatColor.GRAY + "Creates 30 decoy locations",
                ChatColor.GRAY + "Teleports you to a random location",
                ChatColor.GRAY + "Cooldown: 60 seconds",
                ChatColor.DARK_GRAY + "Animation Made By Lusik21556/@Lusik21556"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "getaway_compass"),
                PersistentDataType.BYTE, (byte) 1
        );
        // Animation Made By Lusik21556/@Lusik21556
        compass.setItemMeta(meta);
        player.getInventory().addItem(compass);
        player.sendMessage(ChatColor.GRAY + "You received an " + GETAWAY_NAME + ChatColor.GRAY + ". Right-click to use!");
        return true;
        // Animation Made By Lusik21556/@Lusik21556

    }
}

