package com.customweapons;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.meta.SkullMeta;
import java.lang.reflect.Field;
import java.util.UUID;

public class Util {
    public static class ColorUtil {
        public static String color(String message) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }

    public static void setSkullTexture(SkullMeta meta, String base64) {
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(UUID.randomUUID(), null);
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            propertyClass.getMethod("put", Object.class, Object.class).invoke(
                properties,
                "textures",
                propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64)
            );
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
