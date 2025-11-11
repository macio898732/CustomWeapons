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

public class WardenTearCommand implements CommandExecutor {
    public static final String TEAR_NAME = ChatColor.translateAlternateColorCodes('&',
            "&5&lWarden's Tear");
    public static final String TEAR_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Right-click to shoot a warden beam");
    private static final int CUSTOM_MODEL_DATA = 116;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack tear = new ItemStack(Material.GHAST_TEAR, 1);
        ItemMeta meta = tear.getItemMeta();
        meta.setDisplayName(TEAR_NAME);
        meta.setLore(Arrays.asList(
                TEAR_LORE,
                ChatColor.GRAY + "Deals 10 hearts of damage",
                ChatColor.GRAY + "Cooldown: 45 seconds"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "warden_tear"),
                PersistentDataType.BYTE, (byte) 1
        );
        tear.setItemMeta(meta);
        player.getInventory().addItem(tear);
        player.sendMessage(ChatColor.GRAY + "You received a " + TEAR_NAME + ChatColor.GRAY + ". Right-click to use!");
        return true;
    }
}

