package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class CrownOfAnarchyCommand implements CommandExecutor {
    public static final String CROWN_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&C&B&2&D&3&E&lC&x&C&E&2&F&3&E&lr&x&D&1&3&1&3&D&lo&x&D&4&3&4&3&D&lw&x&D&7&3&6&3&D&ln &x&D&A&3&8&3&C&lO&x&D&D&3&A&3&C&lf &x&E&0&3&C&3&C&lS&x&E&3&3&E&3&B&lt&x&E&6&4&1&3&B&lr&x&E&9&4&3&3&B&li&x&E&C&4&5&3&A&lk&x&E&F&4&7&3&A&le");
    public static final String[] CROWN_LORE = Arrays.stream(new String[]{
            "&8» &7This item is unique!",
            "&8» &7There is only one such crown on the server!",
            "&8» &7It will never be destroyed.",
            "",
            "&6Benefits of the item:",
            "&7Permanent effects:",
            "&7- &bSpeed II",
            "&7- &cFire Resistance I",
            "&7- &4Strength II",
            "&7- &aResistance III",
            "&7- &dLuck I"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 109;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack crown = new ItemStack(Material.GOLDEN_HELMET, 1);
        ItemMeta meta = crown.getItemMeta();
        meta.setDisplayName(CROWN_NAME);
        meta.setLore(Arrays.asList(CROWN_LORE));
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        meta.setUnbreakable(true);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "crown_of_anarchy"),
                PersistentDataType.BYTE, (byte) 1
        );
        crown.setItemMeta(meta);
        player.getInventory().addItem(crown);
        player.sendMessage(ChatColor.GREEN + "You have received the " + CROWN_NAME + ChatColor.GREEN + "!");
        return true;
    }
}