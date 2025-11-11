package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class WitherSkullCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 133;
    public static final String TAG_KEY = "wither_shot";
    private static final String NAME = ChatColor.BLACK + "" + ChatColor.BOLD + "Wither Shot";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this.");
            return true;
        }
        Player p = (Player) sender;
        ItemStack skull = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
        ItemMeta meta = skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(NAME);
            meta.setLore(Arrays.asList(
                    "§8A Skull That Shoots Wither Skulls",
                    "§8Inflicts Wither X for 15s",
                    "§8Cooldown: 45s"
            ));
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            skull.setItemMeta(meta);
        }
        p.getInventory().addItem(skull);
        p.sendMessage(ChatColor.GREEN + "Given a " + NAME);
        return true;
    }
}