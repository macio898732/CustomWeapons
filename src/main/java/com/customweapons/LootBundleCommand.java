package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class LootBundleCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 121;
    public static final String BUNDLE_NAME = ChatColor.translateAlternateColorCodes('&', "&x&A&4&7&3&2&A&lL&x&9&6&6&7&2&6&lo&x&8&8&5&A&2&1&lo&x&7&9&4&E&1&D&lt &x&6&B&4&1&1&8&lB&x&6&3&3&C&1&9&lu&x&6&1&3&E&2&0&ln&x&6&0&4&0&2&6&ld&x&5&E&4&2&2&D&ll&x&5&C&4&4&3&3&le");
    public static final String[] BUNDLE_LORE = new String[] {
            ChatColor.translateAlternateColorCodes('&', "&7Automatically collects enemy loot!"),
            ChatColor.translateAlternateColorCodes('&', "&7Opens like a large chest!")
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack bundle = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = bundle.getItemMeta();
        meta.setDisplayName(BUNDLE_NAME);
        meta.setLore(Arrays.asList(BUNDLE_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.getPersistentDataContainer().set(new NamespacedKey(CustomWeapons.getInstance(), "loot_bundle"), PersistentDataType.BYTE, (byte) 1);
        bundle.setItemMeta(meta);
        player.getInventory().addItem(bundle);
        player.sendMessage(ChatColor.GREEN + "You have received the " + BUNDLE_NAME + ChatColor.GREEN + "!");
        return true;
    }
}