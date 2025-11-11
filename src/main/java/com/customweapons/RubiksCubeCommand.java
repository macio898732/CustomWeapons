package com.customweapons;

import org.bukkit.ChatColor;
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
import java.util.UUID;

public class RubiksCubeCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 125;
    public static final String RUBIK_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&F&F&F&0&0&0&lR&x&F&F&4&C&0&0&lu&x&F&F&9&9&0&0&lb&x&F&F&E&5&0&0&li&x&9&9&F&F&0&0&lk&x&0&0&F&F&0&0&l'&x&0&0&6&6&9&9&ls &x&0&F&0&0&E&6&lC&x&3&C&0&0&9&B&lu&x&6&8&0&0&A&2&lb&x&9&4&0&0&D&3&le");
    public static final String RUBIK_LORE = ChatColor.translateAlternateColorCodes('&',
            "&7Hit a player to scramble their hotbar!\n&7Uses remaining: &a3");
    public static final String TAG_KEY = "rubiks_cube";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack head = player.getInventory().getItemInMainHand();
        if (head.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(ChatColor.RED + "You must be holding a player head!");
            return true;
        }
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(RUBIK_NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Arrays.asList(RUBIK_LORE.split("\n")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        // Add a unique UUID to make it unstackable
        NamespacedKey uniqueKey = new NamespacedKey("customweapons", "unique_id");
        meta.getPersistentDataContainer().set(uniqueKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        head.setItemMeta(meta);
        player.setItemInHand(head);
        player.sendMessage(ChatColor.GREEN + "Your skull has been enchanted with Rubik's powers!");
        return true;
    }
}