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

public class AreaMinerCommand implements CommandExecutor {
    public static final String PICK_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&3&B&3&B&3&B&l3&x&1&E&4&9&4&9&lx&x&0&0&5&7&5&7&l3 &x&4&8&6&C&2&C&lP&x&8&F&8&0&0&0&li&x&8&3&4&0&0&0&lc&x&7&7&0&0&0&0&lk");
    public static final String PICK_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Mines in a 3x3 pattern");
    private static final int CUSTOM_MODEL_DATA = 100;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE, 1);
        ItemMeta meta = pick.getItemMeta();
        meta.setDisplayName(PICK_NAME);
        meta.setLore(Collections.singletonList(PICK_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "area_miner"),
                PersistentDataType.BYTE, (byte) 1
        );
        pick.setItemMeta(meta);
        player.getInventory().addItem(pick);
        player.sendMessage(ChatColor.GREEN + "You received an Area Miner!");
        return true;
    }
}