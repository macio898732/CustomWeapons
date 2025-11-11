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

import java.util.Collections;

public class MaceCommand implements CommandExecutor {
    private final CustomWeapons plugin;

    public MaceCommand(CustomWeapons plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players.");
            return true;
        }

        Player player = (Player) sender;

        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        meta.setDisplayName("§x§7§7§3§5§0§9§lE§x§7§8§3§D§1§6§la§x§7§9§4§6§2§4§lr§x§7§A§4§E§3§1§lt§x§7§B§5§7§3§E§lh §x§7§E§6§8§5§9§lM§x§7§F§7§0§6§6§la§x§8§0§7§9§7§4§lc§x§8§1§8§1§8§1§le");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Right click to unleash an earthquake"));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        mace.setItemMeta(meta);

        player.getInventory().addItem(mace);
        player.sendMessage(ChatColor.GREEN + "Given Mace.");
        return true;
    }
}