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

public class ShockwingCommand implements CommandExecutor {
    public static final String WING_NAME = ChatColor.translateAlternateColorCodes('&',
            "&6&lShockwing");
    public static final String WING_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Charge while flying, create a shockwave on landing");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack wing = new ItemStack(Material.ELYTRA, 1);
        ItemMeta meta = wing.getItemMeta();
        meta.setDisplayName(WING_NAME);
        meta.setLore(Arrays.asList(
                WING_LORE,
                ChatColor.GRAY + "Effect scales with charge %"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        // Add enchantments
        meta.addEnchant(Enchantment.PROTECTION, 4, true);
        meta.addEnchant(Enchantment.UNBREAKING, 4, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "shockwing"),
                PersistentDataType.BYTE, (byte) 1
        );
        wing.setItemMeta(meta);
        player.getInventory().addItem(wing);
        player.sendMessage(ChatColor.GRAY + "You received " + WING_NAME + ChatColor.GRAY + "!");
        return true;
    }
}

