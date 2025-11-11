package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class CupidsBowCommand implements CommandExecutor {
    public static final String BOW_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&F&4&C&4&F&3&lC&x&F&5&B&A&F&4&lu&x&F&6&A&F&F&5&lp&x&F&7&A&5&F&5&li&x&F&8&9&B&F&6&ld&x&F&8&9&0&F&7&l'&x&F&9&8&6&F&8&ls &x&F&A&7&C&F&8&lB&x&F&B&7&1&F&9&lo&x&F&C&6&7&F&A&lw");
    public static final String[] BOW_LORE = Arrays.stream(new String[]{
            "&7Hit players to give them weakness 2 for 7 seconds!"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 111;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta meta = bow.getItemMeta();
        meta.setDisplayName(BOW_NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Arrays.asList(BOW_LORE));
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.INFINITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "cupids_bow"),
                PersistentDataType.BYTE, (byte) 1
        );
        bow.setItemMeta(meta);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
        player.sendMessage(ChatColor.GREEN + "Received Cupid's Bow!");
        return true;
    }
}