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

public class Axe30Command implements CommandExecutor {
    private final CustomWeapons plugin;
    public static final String ITEM_NAME = ChatColor.translateAlternateColorCodes('&', "&c&lExecutioner's Axe");
    public static final String ITEM_LORE = ChatColor.GRAY + "Right-click to steal 33% of a player's health";

    public Axe30Command(CustomWeapons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE, 1);
        ItemMeta meta = axe.getItemMeta();
        meta.setDisplayName(ITEM_NAME);
        meta.setLore(Collections.singletonList(ITEM_LORE));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        meta.setCustomModelData(plugin.getConfig().getInt("axe30.custom_model_data", 101));
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "executioners_axe"),
                PersistentDataType.BYTE, (byte) 1
        );
        axe.setItemMeta(meta);
        player.getInventory().addItem(axe);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.axe30.received", "&aYou received an Executioner's Axe!")));
        return true;
    }
}