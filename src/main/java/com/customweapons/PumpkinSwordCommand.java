package com.customweapons;

import com.customweapons.ColorUtil;
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

public class PumpkinSwordCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 124;
    private final NamespacedKey pumpkinSwordKey;

    public PumpkinSwordCommand(NamespacedKey pumpkinSwordKey) {
        this.pumpkinSwordKey = pumpkinSwordKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        if (!player.hasPermission("pumpkinsword.give")) {
            player.sendMessage(ColorUtil.color("&cYou don't have permission to use this command!"));
            return true;
        }

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(ColorUtil.color("&x&F&F&9&1&0&0&lP&x&F&F&9&1&0&0&lu&x&F&F&9&1&0&0&lm&x&F&F&9&1&0&0&lp&x&F&7&8&D&0&0&lk&x&D&F&8&3&0&0&li&x&C&8&7&8&0&0&ln &x&B&0&6&E&0&0&lS&x&A&9&6&B&0&0&lw&x&A&A&6&C&0&1&lo&x&A&C&6&D&0&1&lr&x&A&D&6&E&0&2&ld"));
        meta.setLore(Arrays.asList(
                ColorUtil.color("&7This magical sword has a 15%% chance"),
                ColorUtil.color("&7to place a pumpkin on your enemy's head!")
        ));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.getPersistentDataContainer().set(pumpkinSwordKey, PersistentDataType.BYTE, (byte) 1);
        sword.setItemMeta(meta);

        player.getInventory().addItem(sword);
        player.sendMessage(ColorUtil.color("&aYou have received the Pumpkin Sword!"));
        return true;
    }
}