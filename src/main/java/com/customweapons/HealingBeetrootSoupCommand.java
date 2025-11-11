package com.customweapons;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Collections;

public class HealingBeetrootSoupCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 120;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("soup.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        ItemStack soup = new ItemStack(Material.BEETROOT_SOUP);
        ItemMeta meta = soup.getItemMeta();
        meta.setDisplayName("§c§lHealing Beetroot Soup");
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Collections.singletonList("§7» §aRight-click to heal yourself to full health!"));
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "healing_soup"),
                PersistentDataType.BYTE, (byte) 1
        );
        soup.setItemMeta(meta);
        player.getInventory().addItem(soup);
        player.sendMessage("§aYou have received the §cHealing Beetroot Soup§a!");
        return true;
    }
}