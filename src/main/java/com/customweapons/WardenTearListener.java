package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class WardenTearListener implements Listener {
    private static final String TEAR_NAME = WardenTearCommand.TEAR_NAME;
    private static final NamespacedKey TEAR_KEY = new NamespacedKey(CustomWeapons.getInstance(), "warden_tear");
    private static final int COOLDOWN_SECONDS = 60;
    private static final long COOLDOWN_MS = COOLDOWN_SECONDS * 1000L;
    private static final double BEAM_RANGE = 20.0;
    private static final double BEAM_DAMAGE = 12.5; // 10 hearts
    private static final double BEAM_STEP = 0.5; // Check every 0.5 blocks
    private static final double KNOCKBACK_STRENGTH = 2.5; // Knockback force

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> activeBeams = new HashSet<>(); // Prevent multiple beams at once

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.GHAST_TEAR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(TEAR_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN_MS) {
            long timeLeft = COOLDOWN_SECONDS - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        // Prevent multiple beams at once
        if (activeBeams.contains(uuid)) {
            player.sendActionBar(ChatColor.RED + "Beam already active!");
            return;
        }

        // WorldGuard check: disallow using ability inside ANY WorldGuard region
        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        CustomWeapons.getInstance().getConfig().getString("messages.warden_tear.disabled_region",
                                "&cYou cannot use this inside a WorldGuard region.")));
                return;
            }
        } catch (Throwable ignored) {
            // If something goes wrong with reflection/worldguard detection, just continue (do not crash plugin).
        }

        // Shoot warden beam
        shootWardenBeam(player);

        // Set cooldown
        cooldowns.put(uuid, now);
    }

    private void shootWardenBeam(Player player) {
        UUID uuid = player.getUniqueId();
        activeBeams.add(uuid);

        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();

        // Play sound
        try {
            player.getWorld().playSound(startLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback sound for older versions
            player.getWorld().playSound(startLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
            player.getWorld().playSound(startLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 2.0f);
        }
        player.sendActionBar(ChatColor.DARK_PURPLE + "Warden's Tear activated!");

        // Create beam effect and damage entities
        new BukkitRunnable() {
            private double distance = 0;
            private final Set<UUID> hitEntities = new HashSet<>();
            private final Location currentLoc = startLoc.clone();

            @Override
            public void run() {
                if (distance >= BEAM_RANGE) {
                    activeBeams.remove(uuid);
                    cancel();
                    return;
                }

                // Move forward
                Vector step = direction.clone().multiply(BEAM_STEP);
                currentLoc.add(step);
                distance += BEAM_STEP;

                // Check for blocks (beam goes through blocks like warden's sonic boom)
                // But we still check for entities

                // Spawn particles - use universally available particles for visibility
                World world = currentLoc.getWorld();
                if (world != null) {
                    // Always visible beam particles (work in all versions)
                    world.spawnParticle(Particle.END_ROD, currentLoc, 3, 0.15, 0.15, 0.15, 0.05);
                    world.spawnParticle(Particle.SOUL, currentLoc, 2, 0.1, 0.1, 0.1, 0.08);
                    
                    // Try newer particles if available (they won't throw exceptions, just won't show if not available)
                    // These are checked via reflection or just attempted - if they fail, nothing breaks
                    try {
                        // Attempt to use sculk particles (1.19+)
                        Particle sculkCharge = Particle.valueOf("SCULK_CHARGE");
                        world.spawnParticle(sculkCharge, currentLoc, 1, 0.08, 0.08, 0.08, 0.05);
                    } catch (Exception ignored) {
                        // Particle doesn't exist, continue with other particles
                    }
                    
                    // Periodic sonic boom effect every 2 blocks
                    if (Math.floor(distance / 2.0) != Math.floor((distance - BEAM_STEP) / 2.0)) {
                        world.spawnParticle(Particle.PORTAL, currentLoc, 5, 0.2, 0.2, 0.2, 0.1);
                        try {
                            Particle sonicBoom = Particle.valueOf("SONIC_BOOM");
                            world.spawnParticle(sonicBoom, currentLoc, 1, 0, 0, 0, 0);
                        } catch (Exception ignored) {
                            // Use alternative visible particle
                            world.spawnParticle(Particle.EXPLOSION, currentLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }

                // Check for entities in a small radius (increased radius for better detection)
                if (world != null) {
                    Collection<Entity> nearbyEntities = world.getNearbyEntities(currentLoc, 1.0, 1.0, 1.0);
                    for (Entity entity : nearbyEntities) {
                        if (entity.equals(player)) continue;
                        if (hitEntities.contains(entity.getUniqueId())) continue; // Already hit this entity
                        if (!(entity instanceof LivingEntity)) continue;

                        LivingEntity target = (LivingEntity) entity;

                        // Check if target is sneaking (warden beams can be avoided by sneaking)
                        if (target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            if (targetPlayer.isSneaking()) {
                                // Beam passes through sneaking players without damage
                                hitEntities.add(entity.getUniqueId());
                                continue;
                            }
                        }

                        // Hit the entity
                        hitEntities.add(entity.getUniqueId());
                        
                        // Deal damage FIRST
                        dealWardenBeamDamage(target, player);
                        
                        // Apply knockback in the direction of the beam
                        Vector knockbackVector = direction.clone().multiply(KNOCKBACK_STRENGTH);
                        // Add slight upward component for better visual effect
                        knockbackVector.setY(knockbackVector.getY() + 0.2);
                        target.setVelocity(target.getVelocity().add(knockbackVector));

                        // Visual and sound effects on hit
                        Location hitLoc = target.getLocation().add(0, 1, 0);
                        
                        // Play hit sound
                        try {
                            world.playSound(hitLoc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.2f);
                        } catch (Exception e) {
                            // Fallback sound
                            world.playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                        }
                        
                        // Hit particles - always visible
                        world.spawnParticle(Particle.DAMAGE_INDICATOR, hitLoc, 15, 0.4, 0.6, 0.4, 0.15);
                        world.spawnParticle(Particle.END_ROD, hitLoc, 20, 0.6, 0.6, 0.6, 0.12);
                        world.spawnParticle(Particle.SOUL, hitLoc, 10, 0.3, 0.5, 0.3, 0.1);
                        
                        // Try sonic boom particle
                        try {
                            Particle sonicBoom = Particle.valueOf("SONIC_BOOM");
                            world.spawnParticle(sonicBoom, hitLoc, 5, 0.5, 0.5, 0.5, 0.1);
                        } catch (Exception e) {
                            // Fallback explosion effect
                            try {
                                Particle explLarge = Particle.valueOf("EXPLOSION_LARGE");
                                world.spawnParticle(explLarge, hitLoc, 1, 0.3, 0.5, 0.3, 0);
                            } catch (Exception e2) {
                                world.spawnParticle(Particle.EXPLOSION, hitLoc, 2, 0.3, 0.5, 0.3, 0);
                            }
                        }
                        
                        // Send message to attacker
                        player.sendActionBar(ChatColor.DARK_PURPLE + "Hit " + 
                            (target instanceof Player ? ((Player) target).getName() : target.getType().name()) + 
                            " with Warden's Tear!");
                        
                        // Continue beam through entities (warden beam can hit multiple)
                    }
                }
            }
        }.runTaskTimer(CustomWeapons.getInstance(), 0L, 1L);
    }

    private void dealWardenBeamDamage(LivingEntity target, Player attacker) {
        try {
            // Determine damage cause - SONIC_BOOM bypasses armor (available in 1.19+)
            EntityDamageEvent.DamageCause damageCause;
            boolean bypassesArmor = false;
            try {
                damageCause = EntityDamageEvent.DamageCause.SONIC_BOOM;
                bypassesArmor = true; // SONIC_BOOM bypasses armor
            } catch (Exception e) {
                // Fallback for older versions
                damageCause = EntityDamageEvent.DamageCause.MAGIC;
                bypassesArmor = false;
            }
            
            // Create damage event with the full damage amount
            EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                    attacker, target, damageCause, BEAM_DAMAGE
            );
            
            // Fire the event so other plugins can modify/cancel it
            Bukkit.getPluginManager().callEvent(damageEvent);
            
            if (damageEvent.isCancelled()) {
                return;
            }
            
            // Get the damage amount - SONIC_BOOM should deal full damage, others get reduced
            double damageToDeal = BEAM_DAMAGE;
            if (!bypassesArmor) {
                // For non-SONIC_BOOM, try to get final damage after armor
                try {
                    double finalDamage = damageEvent.getFinalDamage();
                    if (finalDamage > 0) {
                        damageToDeal = finalDamage;
                    } else {
                        damageToDeal = damageEvent.getDamage();
                    }
                } catch (Exception e) {
                    damageToDeal = damageEvent.getDamage();
                }
            }
            
            // Handle player targets with totem checking
            if (target instanceof Player) {
                Player targetPlayer = (Player) target;
                double currentHealth = targetPlayer.getHealth();
                
                // Check if damage would kill the player
                if (currentHealth <= damageToDeal) {
                    // Check for totem of undying
                    ItemStack offhand = targetPlayer.getInventory().getItemInOffHand();
                    ItemStack mainhand = targetPlayer.getInventory().getItemInMainHand();
                    
                    boolean hasTotem = (offhand != null && offhand.getType() == Material.TOTEM_OF_UNDYING) ||
                                      (mainhand != null && mainhand.getType() == Material.TOTEM_OF_UNDYING);
                    
                    if (hasTotem) {
                        // Activate totem: remove it and set health to 1, give effects
                        if (offhand != null && offhand.getType() == Material.TOTEM_OF_UNDYING) {
                            offhand.setAmount(offhand.getAmount() - 1);
                            if (offhand.getAmount() <= 0) {
                                targetPlayer.getInventory().setItemInOffHand(null);
                            }
                        } else if (mainhand != null && mainhand.getType() == Material.TOTEM_OF_UNDYING) {
                            mainhand.setAmount(mainhand.getAmount() - 1);
                            if (mainhand.getAmount() <= 0) {
                                targetPlayer.getInventory().setItemInMainHand(null);
                            }
                        }
                        
                        // Set health to 1 and apply totem effects
                        targetPlayer.setHealth(1.0);
                        targetPlayer.setFireTicks(0);
                        targetPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.REGENERATION, 900, 1));
                        targetPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 800, 0));
                        targetPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.ABSORPTION, 100, 1));
                        
                        // Play totem activation effects
                        Location loc = targetPlayer.getLocation();
                        targetPlayer.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 100, 0.5, 1.0, 0.5, 0.1);
                        targetPlayer.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                        return;
                    }
                }
            }
            
            // Apply damage directly (SONIC_BOOM bypasses armor, so full damage)
            double currentHealth = target.getHealth();
            double newHealth = Math.max(0, currentHealth - damageToDeal);
            target.setHealth(newHealth);
            
        } catch (Exception e) {
            // Fallback: apply damage directly
            try {
                double currentHealth = target.getHealth();
                target.setHealth(Math.max(0, currentHealth - BEAM_DAMAGE));
            } catch (Exception e2) {
                // Last resort: use standard damage method
                try {
                    target.damage(BEAM_DAMAGE, attacker);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
        activeBeams.remove(uuid);
    }

    private boolean isInWorldGuardRegion(Location loc) {
        try {
            // WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            // adapt location: com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(Location)
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, loc);

            // query.getApplicableRegions(adapted)
            Object applicable = query.getClass().getMethod("getApplicableRegions", adapted.getClass()).invoke(query, adapted);

            // applicable.getRegions().iterator().hasNext() -> returns true if in a region
            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            if (regions instanceof Iterable) {
                return ((Iterable<?>) regions).iterator().hasNext();
            } else {
                // Fallback: try calling size() or isEmpty() by reflection
                try {
                    int size = (int) regions.getClass().getMethod("size").invoke(regions);
                    return size > 0;
                } catch (NoSuchMethodException nsme) {
                    return false;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            // WorldGuard or BukkitAdapter not present
            return false;
        } catch (Throwable t) {
            // Any other reflection error -> treat as "not in region" to avoid blocking functionality
            return false;
        }
    }
}

