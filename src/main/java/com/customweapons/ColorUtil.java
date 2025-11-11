package com.customweapons;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}