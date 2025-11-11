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

import java.util.Arrays;

public class CarrotCrossbowCommand implements CommandExecutor {
    public static final String CROSSBOW_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&D&4&5&9&0&0&lC&x&D&B&6&3&0&6&la&x&E&1&6&E&0&D&lr&x&E&8&7&8&1&3&lr&x&E&E&8&2&1&9&lo&x&F&5&8&D&2&0&lt &x&F&C&9&7&2&6&lC&x&F&F&9&7&2&6&lr&x&F&F&8&D&2&0&lo&x&F&F&8&3&1&9&ls&x&F&F&7&A&1&3&ls&x&F&F&7&0&0&D&lb&x&F&F&6&6&0&6&lo&x&F&F&5&C&0&0&lw");
    public static final String[] CROSSBOW_LORE = Arrays.stream(new String[]{
            "&8» &eThis is an item from the rabbits!",
            "&8» &aThis unique crossbow grants you the amazing ability",
            "&8» &aTo pull players toward you, creating entirely new",
            "&8» &apossibilities for exploration and combat!"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 106;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack crossbow = new ItemStack(Material.CROSSBOW, 1);
        ItemMeta meta = crossbow.getItemMeta();
        meta.setDisplayName(CROSSBOW_NAME);
        meta.setLore(Arrays.asList(CROSSBOW_LORE));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "carrot_crossbow"),
                PersistentDataType.BYTE, (byte) 1
        );
        crossbow.setItemMeta(meta);
        player.getInventory().addItem(crossbow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
        player.sendMessage(ChatColor.GREEN + "You have received a Carrot Crossbow!");
        return true;
    }
}