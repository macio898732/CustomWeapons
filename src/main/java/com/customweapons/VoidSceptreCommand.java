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
import java.util.Collections;

public class VoidSceptreCommand implements CommandExecutor {
    public static final String SCEPTRE_NAME = ChatColor.translateAlternateColorCodes('&',
            "&8&lVoid Sceptre");
    public static final String SCEPTRE_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Right-click to summon a black hole");
    private static final int CUSTOM_MODEL_DATA = 115;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack sceptre = new ItemStack(Material.DIAMOND_HOE, 1);
        ItemMeta meta = sceptre.getItemMeta();
        meta.setDisplayName(SCEPTRE_NAME);
        meta.setLore(Arrays.asList(
                SCEPTRE_LORE,
                ChatColor.GRAY + "Cooldown: 100 seconds"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "void_sceptre"),
                PersistentDataType.BYTE, (byte) 1
        );
        sceptre.setItemMeta(meta);
        player.getInventory().addItem(sceptre);
        player.sendMessage(ChatColor.GRAY + "You received a " + SCEPTRE_NAME + ChatColor.GRAY + ". Right-click to use!");
        return true;
    }
}

