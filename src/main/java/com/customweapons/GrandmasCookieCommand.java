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

public class GrandmasCookieCommand implements CommandExecutor {
    public static final String COOKIE_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&A&1&6&A&0&0&lG&x&9&5&6&0&0&0&lr&x&8&8&5&6&0&0&la&x&7&C&4&C&0&0&ln&x&6&F&4&2&0&0&ld&x&6&3&3&8&0&0&lm&x&5&6&2&E&0&0&la&x&4&A&2&4&0&0&l'&x&5&6&2&F&0&0&ls &x&6&3&3&B&0&0&lC&x&6&F&4&6&0&0&lo&x&7&B&5&1&0&0&lo&x&8&7&5&C&0&0&lk&x&9&4&6&8&0&0&li&x&A&0&7&3&0&0&le");
    public static final String COOKIE_LORE = ChatColor.translateAlternateColorCodes('&',
            "&8Gives &6Strength &7III &8and &3Speed &7III &8for 10 seconds");
    private static final int CUSTOM_MODEL_DATA = 117;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack cookie = new ItemStack(Material.COOKIE, 1);
        ItemMeta meta = cookie.getItemMeta();
        meta.setDisplayName(COOKIE_NAME);
        meta.setLore(Collections.singletonList(COOKIE_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "grandmas_cookie"),
                PersistentDataType.BYTE, (byte) 1
        );
        cookie.setItemMeta(meta);
        player.getInventory().addItem(cookie);
        player.sendMessage(ChatColor.GREEN + "You received a special cookie from Grandma!");
        return true;
    }
}