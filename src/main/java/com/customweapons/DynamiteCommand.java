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
import java.util.Collections;

public class DynamiteCommand implements CommandExecutor {
    public static final String DYNAMITE_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&E&1&0&0&0&0&lD&x&B&7&0&0&0&0&ly&x&8&E&0&0&0&0&ln&x&A&3&0&0&0&0&la&x&D&9&0&0&0&0&lm&x&F&C&0&0&0&0&li&x&F&E&0&0&0&0&lt&x&F&F&0&0&0&0&le");
    public static final String DYNAMITE_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7A highly explosive device");
    private static final int CUSTOM_MODEL_DATA = 114;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack dynamite = new ItemStack(Material.RED_CANDLE, 1);
        ItemMeta meta = dynamite.getItemMeta();
        meta.setDisplayName(DYNAMITE_NAME);
        meta.setLore(Collections.singletonList(DYNAMITE_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "dynamite"),
                PersistentDataType.BYTE, (byte) 1
        );
        dynamite.setItemMeta(meta);
        player.getInventory().addItem(dynamite);
        player.sendMessage(ChatColor.GRAY + "You received a " + DYNAMITE_NAME + ChatColor.GRAY + ". Right-click to throw!");
        return true;
    }
}