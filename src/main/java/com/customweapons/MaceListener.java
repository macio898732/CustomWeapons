package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MaceListener implements Listener {
    private final Plugin plugin;

    private final Map<UUID, Long> lastUse = new HashMap<>();
    private static final long COOLDOWN_MS = 120_000L; // 75 seconds

    public MaceListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String maceName = "§x§7§7§3§5§0§9§lE§x§7§8§3§D§1§6§la§x§7§9§4§6§2§4§lr§x§7§A§4§E§3§1§lt§x§7§B§5§7§3§E§lh §x§7§E§6§8§5§9§lM§x§7§F§7§0§6§6§la§x§8§0§7§9§7§4§lc§x§8§1§8§1§8§1§le";
        if (!meta.getDisplayName().equals(maceName)) return;

        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        final Player player = event.getPlayer();

        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage("§cYou cannot use the Mace inside a WorldGuard region.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.2f);
                event.setCancelled(true);
                return;
            }
        } catch (Throwable ignored) {
        }

        UUID uid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastUse.get(uid);
        if (last != null) {
            long passed = now - last;
            if (passed < COOLDOWN_MS) {
                long remainingMs = COOLDOWN_MS - passed;
                long secs = (remainingMs + 999) / 1000;
                player.sendActionBar("§cMace is on cooldown: " + secs + "s remaining.");
                event.setCancelled(true);
                return;
            }
        }

        lastUse.put(uid, now);

        event.setCancelled(true);
        final World world = player.getWorld();

        player.setNoDamageTicks(200);
        player.setFallDistance(0f);

        Location loc = player.getLocation();
        world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.1f);
        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 0.6, 0), 12, 0.4, 0.6, 0.4, 0.02);

        player.setVelocity(new Vector(0, 2.4, 0));


        new BukkitRunnable() {
            @Override
            public void run() {

                world.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.9f);
                player.setVelocity(new Vector(0, -4.0, 0));
                player.setFallDistance(0f);
                world.spawnParticle(Particle.EXPLOSION, player.getLocation().clone().add(0, 0.1, 0), 6, 0.3, 0.2, 0.3, 0.02);

                new BukkitRunnable() {
                    int ticksWaited = 0;
                    final int maxWait = 40;

                    @Override
                    public void run() {
                        if (!player.isValid() || !player.isOnline()) {
                            cancel();
                            return;
                        }

                        boolean onGround = player.isOnGround();
                        if (!onGround) {
                            try {
                                onGround = player.getLocation().getBlock().getType().isSolid();
                            } catch (Exception ignored) { }
                        }

                        if (onGround || ticksWaited++ >= maxWait) {
                            Location impactLoc = player.getLocation().clone();
                            // impact sounds & particles
                            world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.9f);
                            world.playSound(impactLoc, Sound.ENTITY_PLAYER_BIG_FALL, 0.9f, 1.0f);
                            world.spawnParticle(Particle.CLOUD, impactLoc.clone().add(0, 0.1, 0), 12, 0.5, 0.2, 0.5, 0.02);

                            applyLandingDamage(player, impactLoc);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    simulateEarthquake(player);
                                }
                            }.runTaskLater(plugin, 1L);

                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }.runTaskLater(plugin, 40L);
    }

    private void simulateEarthquake(Player caster) {
        World w = caster.getWorld();
        Location center = caster.getLocation().clone().add(0, 0.1, 0);

        DustOptions dust = new DustOptions(Color.fromRGB(150, 120, 90), 1.2F);

        double maxRadius = 5.0;
        double step = 0.6;
        int pointsPerRing = 36;
        for (double r = 0.5; r <= maxRadius; r += step) {
            for (int layer = 0; layer < 4; layer++) {
                double y = 0.1 + layer * 0.2;
                for (int i = 0; i < pointsPerRing; i++) {
                    double angle = 2 * Math.PI * i / pointsPerRing;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    Location spawn = center.clone().add(x, y + (Math.random() * 0.3 - 0.15), z);

                    w.spawnParticle(Particle.DUST, spawn, 1, 0, 0, 0, 0.0, dust);

                    if (Math.random() < 0.22) {
                        w.spawnParticle(Particle.BLOCK_CRUMBLE, spawn, 3, 0.04, 0.04, 0.04, 0.04, Material.DIRT.createBlockData());
                    }
                }
            }
        }

        w.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f);
        w.playSound(center, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.85f);

        caster.setNoDamageTicks(60);
    }

    private void applyLandingDamage(Player caster, Location center) {
        final double damageAmount = 100.0; // damage amount (before armor reduction)
        final double effectRadius = 5.0;
        World w = caster.getWorld();

        for (Entity e : caster.getNearbyEntities(effectRadius, effectRadius, effectRadius)) {
            if (!(e instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) e;
            if (le.equals(caster)) continue;

            if (le instanceof Player) {
                Player p = (Player) le;
                if (p.getGameMode() == GameMode.CREATIVE || p.isInvulnerable()) continue;
            } else {
                if (le.isInvulnerable()) continue;
            }

            Vector dir = le.getLocation().toVector().subtract(center.toVector());
            dir.setY(0);
            if (dir.lengthSquared() < 0.0001) dir = new Vector(Math.random() - 0.5, 0, Math.random() - 0.5);
            double distance = Math.max(0.2, le.getLocation().distance(center));
            dir.normalize();
            double strength = 1.6 * (1.0 - Math.min(distance / effectRadius, 1.0));
            dir.multiply(strength);
            dir.setY(0.6);
            le.setVelocity(dir);
            le.setFallDistance(0f);

            w.playSound(le.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f);
            w.spawnParticle(Particle.DAMAGE_INDICATOR, le.getLocation().add(0, 1, 0), 6, 0.2, 0.2, 0.2, 0.01);

            dealRawDamage(le, damageAmount, caster);
        }

        caster.setNoDamageTicks(60);
    }

    private void dealRawDamage(LivingEntity target, double amount, Player attacker) {
        // Use standard damage method which respects armor
        // The damage() method will automatically create EntityDamageByEntityEvent
        // and apply armor reduction
        target.damage(amount, attacker);
    }

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

                    if (id == null) return true;

                    if (!"koth".equalsIgnoreCase(id)) return true;
                }


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