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

public class FlyRodCommand implements CommandExecutor {
    public static final String ROD_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&0&C&9&9&F&F&lF&x&2&C&A&7&F&F&ll&x&4&D&B&4&F&F&ly&x&6&D&C&2&F&F&le&x&8&E&C&F&F&F&lr&x&A&E&D&D&F&F&ls &x&C&E&E&B&F&F&lF&x&E&F&F&8&F&F&li&x&E&E&F&C&F&A&ls&x&C&C&F&6&F&1&lh&x&A&A&F&0&E&7&li&x&8&8&E&A&D&E&ln&x&6&6&E&3&D&4&lg &x&4&4&D&D&C&B&lR&x&2&2&D&7&C&1&lo&x&0&0&D&1&B&8&ld");
    public static final String ROD_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Hit a player to disable their Elytra for 20s!");
    private static final int CUSTOM_MODEL_DATA = 116;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack rod = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta meta = rod.getItemMeta();
        meta.setDisplayName(ROD_NAME);
        meta.setLore(Collections.singletonList(ROD_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "fly_rod"),
                PersistentDataType.BYTE, (byte) 1
        );
        rod.setItemMeta(meta);
        player.getInventory().addItem(rod);
        player.sendMessage(ChatColor.GREEN + "You have received " + ROD_NAME + ChatColor.GREEN + "!");
        return true;
    }
}