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

public class BunnySwordCommand implements CommandExecutor {
    public static final String SWORD_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&0&0&8&5&E&6&lB&x&0&0&8&9&E&7&lu&x&0&0&8&C&E&7&ln&x&0&0&9&0&E&8&ln&x&0&0&9&3&E&9&ly &x&0&0&9&7&E&9&lS&x&0&0&9&A&E&A&lw&x&0&0&9&E&E&B&lo&x&0&0&A&1&E&B&lr&x&0&0&A&5&E&C&ld");
    public static final String SWORD_LORE = ChatColor.translateAlternateColorCodes('&',
            "&8» &aWhen you hit an opponent, it blocks their ability \n&8» &ato jump for &64 seconds&a!");
    private static final int CUSTOM_MODEL_DATA = 105;

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
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "bunny_sword"),
                PersistentDataType.BYTE, (byte) 1
        );
        sword.setItemMeta(meta);
        player.getInventory().addItem(sword);
        player.sendMessage(ChatColor.GREEN + "You have received the " + SWORD_NAME + ChatColor.GREEN + "!");
        return true;
    }
}