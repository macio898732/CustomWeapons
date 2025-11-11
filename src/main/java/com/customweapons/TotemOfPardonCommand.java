package com.customweapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class TotemOfPardonCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 129;
    public static final String TAG_KEY = "totem_of_pardon";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("totemofpardon.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        meta.setDisplayName("§6§lTotem of Pardon");
        meta.setLore(Arrays.asList(
                "§7A sacred item that prevents",
                "§7inventory loss upon death",
                "",
                "§c⚠ One-time use only"
        ));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        totem.setItemMeta(meta);
        player.getInventory().addItem(totem);
        player.sendMessage("§aYou have received a §6Totem of Pardon§a!");
        return true;
    }
}