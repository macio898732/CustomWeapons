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
import java.util.Collections;

public class UnbreakableWallCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 131;
    public static final String TAG_KEY = "unbreakable_wall";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("shield.get")) {
            player.sendMessage("§cYou don't have permission to use this!");
            return true;
        }
        ItemStack shield = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = shield.getItemMeta();
        meta.setDisplayName("§x§F§B§E§2§0§0§lG§x§F§C§D§D§0§0§lo§x§F§D§D§7§0§0§ld§x§F§D§D§2§0§0§ll§x§F§E§C§C§0§0§ly §x§F§F§C§7§0§0§lH§x§F§F§C§D§0§0§la§x§F§F§D§3§0§0§lm§x§F§F§D§A§0§0§lm§x§F§F§E§0§0§0§le§x§F§F§E§6§0§0§lr");
        meta.setLore(Collections.singletonList("§7Right-click to become invincible for 10 seconds"));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        shield.setItemMeta(meta);
        player.getInventory().addItem(shield);
        player.sendMessage("§aYou received the Godly Hammer!");
        return true;
    }
}