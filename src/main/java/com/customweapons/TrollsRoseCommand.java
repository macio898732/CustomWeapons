package com.customweapons;

import com.customweapons.ColorUtil;
import org.bukkit.Bukkit;
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

public class TrollsRoseCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 130;
    private final NamespacedKey roseKey;

    public TrollsRoseCommand(NamespacedKey roseKey) {
        this.roseKey = roseKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!player.hasPermission("trollsrose.give")) {
            player.sendMessage(ColorUtil.color("&cYou don't have permission to use this command!"));
            return true;
        }

        ItemStack rose = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = rose.getItemMeta();
        meta.setDisplayName(ColorUtil.color("&x&0&0&9&9&2&B&lT&x&0&D&9&1&2&B&lr&x&1&9&8&9&2&C&lo&x&2&6&8&2&2&C&ll&x&3&2&7&A&2&D&ll&x&3&F&7&2&2&D&l'&x&4&B&6&A&2&D&ls &x&5&8&6&2&2&E&lr&x&6&4&5&B&2&E&lo&x&7&1&5&3&2&F&ls&x&7&D&4&B&2&F&le"));
        meta.setLore(Arrays.asList(
                ColorUtil.color("&7» &eHold this rose to gain"),
                ColorUtil.color("&7» &aResistance I &eand &aRegeneration I &einfinite!")
        ));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.getPersistentDataContainer().set(roseKey, PersistentDataType.BYTE, (byte) 1);
        rose.setItemMeta(meta);

        player.getInventory().addItem(rose);
        player.sendMessage(ColorUtil.color("&aYou have received the &x&0&0&9&9&2&B&lT&x&0&D&9&1&2&B&lr&x&1&9&8&9&2&C&lo&x&2&6&8&2&2&C&ll&x&3&2&7&A&2&D&ll&x&3&F&7&2&2&D&l'&x&4&B&6&A&2&D&ls &x&5&8&6&2&2&E&lr&x&6&4&5&B&2&E&lo&x&7&1&5&3&2&F&ls&x&7&D&4&B&2&F&le&a!"));
        return true;
    }
}