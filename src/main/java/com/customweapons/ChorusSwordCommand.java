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

public class ChorusSwordCommand implements CommandExecutor {
    public static final String SWORD_NAME = ChatColor.translateAlternateColorCodes('&', "&x&F&0&1&3&D&A&lC&x&F&5&1&D&E&3&lh&x&F&9&2&6&E&B&lo&x&F&E&3&0&F&4&lr&x&E&B&2&F&D&F&lu&x&C&E&2&9&B&C&ls &x&B&0&2&3&9&9&lS&x&A&6&2&0&8&E&lw&x&C&2&2&2&B&3&lo&x&D&F&2&5&D&9&lr&x&F&B&2&7&F&F&ld");
    public static final String[] SWORD_LORE = Arrays.stream(new String[]{
            "&7» &eThis sword freezes your enemies!",
            "&7» &aWhen you hit a player, they are completely frozen",
            "&7» &afor &63 seconds&a!"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 107;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(SWORD_NAME);
        meta.setLore(Arrays.asList(SWORD_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "chorus_sword"),
                PersistentDataType.BYTE, (byte) 1
        );
        sword.setItemMeta(meta);
        player.getInventory().addItem(sword);
        player.sendMessage(ChatColor.GREEN + "You have received a Chorus Sword!");
        return true;
    }
}