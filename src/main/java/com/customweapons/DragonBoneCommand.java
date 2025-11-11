package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;

import java.util.Arrays;

public class DragonBoneCommand implements CommandExecutor {
    public static final String TAG_KEY = "dragon_bone";
    private static final String NAME = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Dragon Bone";
    private static final int CUSTOM_MODEL_DATA = 112;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this.");
            return true;
        }
        Player p = (Player) sender;
        ItemStack bone = new ItemStack(Material.BONE, 1);
        ItemMeta meta = bone.getItemMeta();
        meta.setLore(Arrays.asList(
                "§8Can Instant Teleport up to 12 Blocks",
                "§8Very Rare Drop",
                "§8Cooldown: 30s"
        ));
        meta.setDisplayName(NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        bone.setItemMeta(meta);
        p.getInventory().addItem(bone);
        p.sendMessage(ChatColor.GREEN + "Given a " + NAME);
        return true;
    }
}