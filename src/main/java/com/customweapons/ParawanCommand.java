package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class ParawanCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 122;
    public static final String PARAWAN_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&F&F&B&0&0&0&lU&x&F&0&C&7&0&3&lm&x&E&0&D&E&0&6&lb&x&C&4&D&C&0&5&lr&x&A&2&C&E&0&2&le&x&7&7&C&8&0&A&ll&x&3&C&D&4&2&7&ll&x&0&0&E&0&4&5&la");
    public static final String[] PARAWAN_LORE = Arrays.stream(new String[]{
            "&7When used, this item pushes",
            "&7away all nearby enemies!"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack feather = new ItemStack(Material.FEATHER, 1);
        ItemMeta meta = feather.getItemMeta();
        meta.setDisplayName(PARAWAN_NAME);
        meta.setLore(Arrays.asList(PARAWAN_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "parawan"),
                PersistentDataType.BYTE, (byte) 1
        );
        feather.setItemMeta(meta);
        player.getInventory().addItem(feather);
        player.sendMessage(ChatColor.GRAY + "You received a " + PARAWAN_NAME + ChatColor.GRAY + "!");
        return true;
    }
}