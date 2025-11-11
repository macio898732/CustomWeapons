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
import java.util.Collections;

public class GravityAxeCommand implements CommandExecutor {
    public static final String AXE_NAME = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Gravity Axe";
    public static final String AXE_LORE = ChatColor.GRAY + "Right-click to pull enemies closer";
    private static final int CUSTOM_MODEL_DATA = 119;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE, 1);
        ItemMeta meta = axe.getItemMeta();
        meta.setDisplayName(AXE_NAME);
        meta.setLore(Collections.singletonList(AXE_LORE));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "gravity_axe"),
                PersistentDataType.BYTE, (byte) 1
        );
        axe.setItemMeta(meta);
        player.getInventory().addItem(axe);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You received the Gravity Axe!");
        return true;
    }
}