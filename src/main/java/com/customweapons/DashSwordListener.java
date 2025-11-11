package com.customweapons;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.Particle;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Set;

public class DashSwordListener implements Listener {

    private final NamespacedKey key;
    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final long cooldownMillis = 7000L;

    public DashSwordListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "dash_sword");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getItem() == null) return;
        if (!event.getItem().hasItemMeta()) return;

        String tag = event.getItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (tag == null || !tag.equals("dash_sword")) return;

        Player player = event.getPlayer();

        // WorldGuard reflection check (same approach as Axe30Listener)
        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage("§cYou cannot use the Dash Sword inside a WorldGuard region.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
                return;
            }
        } catch (Throwable ignored) {
            // If reflection fails, allow usage to avoid crashing plugin
        }

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long expire = cooldowns.get(id);
        if (expire != null && expire > now) {
            long secs = (expire - now + 999) / 1000;
            player.sendActionBar("§cDᴀsʜ ɪs ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ ғᴏʀ " + secs + "s.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
            return;
        }

        // apply dash
        Vector dir = player.getLocation().getDirection().clone().normalize();
        dir.setY(Math.max(dir.getY(), 0.05));
        Vector dash = dir.multiply(1.8);
        player.setVelocity(dash);

        // effects
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 12, 0.4, 0.4, 0.4);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.2f);

        // ram behavior: deal damage to entities hit during the dash (respects armor)
        final Set<UUID> hitThisDash = new HashSet<>();
        final double ramDamage = 25.0; // damage amount (4 hearts, before armor reduction)
        final int durationTicks = 12; // check duration (~0.6s)

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > durationTicks || !player.isValid() || player.isDead()) {
                    cancel();
                    return;
                }

                for (Entity e : player.getNearbyEntities(2.0, 1.5, 2.0)) {
                    if (!(e instanceof LivingEntity)) continue;
                    if (e.getUniqueId().equals(player.getUniqueId())) continue;
                    if (hitThisDash.contains(e.getUniqueId())) continue;

                    // ensure entity is roughly in front of the player
                    Vector toEntity = e.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (toEntity.lengthSquared() == 0) continue;
                    toEntity.normalize();
                    double dot = toEntity.dot(dir);
                    if (dot < 0.5) continue;

                    LivingEntity le = (LivingEntity) e;
                    if (le.isDead()) continue;

                    // apply damage that respects armor
                    le.damage(ramDamage, player);

                    // small knockback
                    Vector knock = dir.clone().multiply(0.8);
                    knock.setY(0.4);
                    le.setVelocity(knock);

                    // feedback
                    le.getWorld().spawnParticle(Particle.CRIT, le.getLocation().add(0, 0.5, 0), 8, 0.2, 0.2, 0.2);
                    le.getWorld().playSound(le.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);

                    hitThisDash.add(le.getUniqueId());
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // set cooldown
        cooldowns.put(id, now + cooldownMillis);
    }

    // Reflection-based WorldGuard region check (copied from Axe30Listener)
    private boolean isInWorldGuardRegion(Location loc) {
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, loc);

            Object applicable = query.getClass().getMethod("getApplicableRegions", adapted.getClass()).invoke(query, adapted);

            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            if (regions instanceof Iterable) {
                boolean foundAny = false;
                for (Object region : (Iterable<?>) regions) {
                    foundAny = true;
                    String id = null;
                    try {
                        Object idObj = region.getClass().getMethod("getId").invoke(region);
                        if (idObj != null) id = idObj.toString();
                    } catch (NoSuchMethodException ignored) {}

                    // if we can't determine the id, treat it as a blocking region
                    if (id == null) return true;
                    // if any region is not named "koth", block usage
                    if (!"koth".equalsIgnoreCase(id)) return true;
                }
                // if there were no regions -> not inside a WG region (allow)
                // if all regions are named "koth" -> allow
                return false;
            } else {
                try {
                    int size = (int) regions.getClass().getMethod("size").invoke(regions);
                    if (size == 0) return false;
                    for (int i = 0; i < size; i++) {
                        Object region = regions.getClass().getMethod("get", int.class).invoke(regions, i);
                        String id = null;
                        try {
                            Object idObj = region.getClass().getMethod("getId").invoke(region);
                            if (idObj != null) id = idObj.toString();
                        } catch (NoSuchMethodException ignored) {}
                        if (id == null) return true;
                        if (!"koth".equalsIgnoreCase(id)) return true;
                    }
                    return false;
                } catch (NoSuchMethodException nsme) {
                    return false;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }
}