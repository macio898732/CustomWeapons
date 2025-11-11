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

public class ExplosiveChargeCommand implements CommandExecutor {
    public static final String CHARGE_NAME = "§x§2§3§9§6§2§0§lE§x§2§3§9§6§2§0§lx§x§2§3§9§6§2§0§lp§x§2§3§9§6§2§0§ll§x§2§3§9§6§2§0§lo§x§2§3§9§6§2§0§ls§x§2§3§9§6§2§0§li§x§2§3§9§6§2§0§lv§x§2§3§9§6§2§0§le §x§2§3§9§6§2§0§lC§x§2§3§9§6§2§0§lh§x§2§3§9§6§2§0§la§x§2§3§9§6§2§0§lr§x§2§3§9§6§2§0§lg§x§2§3§9§6§2§0§le";
    public static final String TAG_KEY = "explosive_charge";
    private static final int CUSTOM_MODEL_DATA = 115;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("explosivecharge.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        ItemStack charge = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = charge.getItemMeta();
        meta.setDisplayName(CHARGE_NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Collections.singletonList("§7A powerful explosive charge!"));
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        charge.setItemMeta(meta);
        player.getInventory().addItem(charge);
        player.sendMessage("§aYou have received an Explosive Charge!");
        return true;
    }
}