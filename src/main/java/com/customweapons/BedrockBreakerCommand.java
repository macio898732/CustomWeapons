package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Collections;

public class BedrockBreakerCommand implements CommandExecutor {
    private final CustomWeapons plugin;
    public static final String ITEM_NAME = ChatColor.translateAlternateColorCodes('&', "&x&2&E&2&D&2&D&lB&x&2&E&2&C&2&D&le&x&2&D&2&C&2&C&ld&x&2&D&2&B&2&C&lr&x&2&C&2&A&2&B&lo&x&2&C&2&B&2&D&lc&x&2&B&2&D&2&F&lk &x&2&A&3&0&3&1&lB&x&2&9&3&2&3&3&lr&x&2&B&3&5&3&6&le&x&3&2&3&9&3&B&la&x&3&9&3&E&3&F&lk&x&4&0&4&2&4&3&le&x&4&7&4&7&4&7&lr");
    public static final String ITEM_LORE = ChatColor.GRAY + "Right click to destroy bedrock!";
    private static final int CUSTOM_MODEL_DATA = 103;

    public BedrockBreakerCommand(CustomWeapons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack pickaxe = new ItemStack(Material.WOODEN_PICKAXE, 1);
        ItemMeta meta = pickaxe.getItemMeta();
        meta.setDisplayName(ITEM_NAME);
        meta.setLore(Collections.singletonList(ITEM_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                plugin.getBedrockBreakerKey(),
                PersistentDataType.BYTE, (byte) 1
        );
        pickaxe.setItemMeta(meta);
        player.getInventory().addItem(pickaxe);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bedrockbreaker.received", "&aYou received a &6Bedrock Breaker!")));
        return true;
    }
}