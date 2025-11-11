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

public class BloodthirsterCommand implements CommandExecutor {
    public static final String SWORD_NAME = ChatColor.translateAlternateColorCodes('&', "&x&8&0&0&0&0&0&lB&x&7&D&0&0&0&0&ll&x&7&A&0&0&0&0&lo&x&7&6&0&0&0&0&lo&x&7&3&0&0&0&0&ld &x&6&C&0&0&0&0&lT&x&6&5&0&0&0&0&lh&x&5&E&0&0&0&0&li&x&5&7&0&0&0&0&lr&x&5&6&0&0&0&0&ls&x&5&6&0&0&0&0&lt&x&5&5&0&0&0&0&e&x&5&4&0&0&0&0&r");
    public static final String SWORD_LORE = ChatColor.translateAlternateColorCodes('&', "&7Has a 30% chance to steal health from enemies");
    private static final int CUSTOM_MODEL_DATA = 104;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(SWORD_NAME);
        meta.setLore(Collections.singletonList(SWORD_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "bloodthirster_sword"),
                PersistentDataType.BYTE, (byte) 1
        );
        sword.setItemMeta(meta);
        player.getInventory().addItem(sword);
        player.sendMessage(ChatColor.GREEN + "You received the " + SWORD_NAME + ChatColor.GREEN + " sword!");
        return true;
    }
}