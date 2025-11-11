package com.customweapons;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

// Import all your command and listener classes here
import com.customweapons.BedrockBreakerCommand;
import com.customweapons.Axe30Command;
import com.customweapons.LootBundleCommand;
import com.customweapons.BloodthirsterCommand;
import com.customweapons.CarrotCrossbowCommand;
import com.customweapons.BunnySwordCommand;
import com.customweapons.DashSwordCommand;
import com.customweapons.DashSwordListener;
import com.customweapons.ChorusSwordCommand;
import com.customweapons.CoffeeCommand;
import com.customweapons.CrownOfAnarchyCommand;
import com.customweapons.GrandmasCookieCommand;
import com.customweapons.RubiksCubeCommand;
import com.customweapons.CubanSkullCommand;
import com.customweapons.DynamiteCommand;
import com.customweapons.CupidsBowCommand;
import com.customweapons.ExplosiveChargeCommand;
import com.customweapons.DragonFireworkCommand;
import com.customweapons.GrapplingHookCommand;
import com.customweapons.FlyRodCommand;
import com.customweapons.ParawanCommand;
import com.customweapons.GravityAxeCommand;
import com.customweapons.PoisonPencilCommand;
import com.customweapons.AreaMinerCommand;
import com.customweapons.TrollsRoseCommand;
import com.customweapons.PumpkinSwordCommand;
import com.customweapons.ScytheListener;
import com.customweapons.ScytheCommand;
import com.customweapons.BalloonListener;
import com.customweapons.BalloonCommand;
import com.customweapons.DragonBoneCommand;
import com.customweapons.WitherSkullCommand;
import com.customweapons.VoidSceptreCommand;
import com.customweapons.WardenTearCommand;
import com.customweapons.ShockwingCommand;
import com.customweapons.GetAwayCommand;

import com.customweapons.BedrockBreakerListener;
import com.customweapons.Axe30Listener;
import com.customweapons.LootBundleListener;
import com.customweapons.BloodthirsterListener;
import com.customweapons.CarrotCrossbowListener;
import com.customweapons.BunnySwordListener;
import com.customweapons.ChorusSwordListener;
import com.customweapons.CoffeeListener;
import com.customweapons.CrownOfAnarchyListener;
import com.customweapons.GrandmasCookieListener;
import com.customweapons.RubiksCubeListener;
import com.customweapons.CubanSkullListener;
import com.customweapons.DynamiteListener;
import com.customweapons.CupidsBowListener;
import com.customweapons.ExplosiveChargeListener;
import com.customweapons.DragonFireworkListener;
import com.customweapons.GrapplingHookListener;
import com.customweapons.FlyRodListener;
import com.customweapons.ParawanListener;
import com.customweapons.GravityAxeListener;
import com.customweapons.PoisonPencilListener;
import com.customweapons.AreaMinerListener;
import com.customweapons.TrollsRoseListener;
import com.customweapons.PumpkinSwordListener;
import com.customweapons.DragonBoneListener;
import com.customweapons.WitherSkullListener;
import com.customweapons.VoidSceptreListener;
import com.customweapons.WardenTearListener;
import com.customweapons.ShockwingListener;
import com.customweapons.GetAwayListener;

// Add any additional imports for new items/listeners here

public class CustomWeapons extends JavaPlugin {
    private static CustomWeapons instance;

    // NamespacedKeys for all custom items
    private NamespacedKey trollsRoseKey, pumpkinSwordKey, bedrockBreakerKey, axe30Key, lootBundleKey, bloodthirsterKey,
            carrotCrossbowKey, bunnySwordKey, chorusSwordKey, coffeeKey, crownOfAnarchyKey, grandmasCookieKey,
            rubiksCubeKey, cubanSkullKey, dynamiteKey, cupidsBowKey, explosiveChargeKey, dragonFireworkKey,
            grapplingHookKey, flyRodKey, parawanKey, gravityAxeKey, poisonPencilKey, areaMinerKey,
            healingBeetrootSoupKey, totemOfPardonKey, tempestHammerKey, unbreakableWallKey, warmMilkKey, swapBallKey, ScytheKey, BalloonKey, WitherSkullKey, DragonBoneKey, dashSwordKey, wardenTearKey, shockwingKey, getawayCompassKey;

    public static CustomWeapons getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize NamespacedKeys for all custom items
        trollsRoseKey = new NamespacedKey(this, "trolls_rose");
        pumpkinSwordKey = new NamespacedKey(this, "pumpkin_sword");
        bedrockBreakerKey = new NamespacedKey(this, "bedrock_breaker");
        axe30Key = new NamespacedKey(this, "executioners_axe");
        lootBundleKey = new NamespacedKey(this, "loot_bundle");
        bloodthirsterKey = new NamespacedKey(this, "bloodthirster_sword");
        carrotCrossbowKey = new NamespacedKey(this, "carrot_crossbow");
        bunnySwordKey = new NamespacedKey(this, "bunny_sword");
        chorusSwordKey = new NamespacedKey(this, "chorus_sword");
        coffeeKey = new NamespacedKey(this, "radioactive_coffee");
        crownOfAnarchyKey = new NamespacedKey(this, "crown_of_anarchy");
        grandmasCookieKey = new NamespacedKey(this, "grandmas_cookie");
        rubiksCubeKey = new NamespacedKey(this, "rubiks_cube");
        cubanSkullKey = new NamespacedKey(this, "cuban_skull");
        dynamiteKey = new NamespacedKey(this, "dynamite");
        cupidsBowKey = new NamespacedKey(this, "cupids_bow");
        explosiveChargeKey = new NamespacedKey(this, "explosive_charge");
        dragonFireworkKey = new NamespacedKey(this, "dragon_firework");
        grapplingHookKey = new NamespacedKey(this, "grappling_hook");
        flyRodKey = new NamespacedKey(this, "fly_rod");
        parawanKey = new NamespacedKey(this, "parawan");
        gravityAxeKey = new NamespacedKey(this, "gravity_axe");
        poisonPencilKey = new NamespacedKey(this, "poison_pencil");
        areaMinerKey = new NamespacedKey(this, "area_miner");
        healingBeetrootSoupKey = new NamespacedKey(this, "healing_soup");
        totemOfPardonKey = new NamespacedKey(this, "totem_of_pardon");
        tempestHammerKey = new NamespacedKey(this, "tempest_hammer");
        unbreakableWallKey = new NamespacedKey(this, "unbreakable_wall");
        warmMilkKey = new NamespacedKey(this, "warm_milk");
        swapBallKey = new NamespacedKey(this, "swap_ball");
        ScytheKey = new NamespacedKey(this, "blind_scythe");
        BalloonKey = new NamespacedKey(this, "balloon");
        WitherSkullKey = new NamespacedKey(this, "wither_shot");
        DragonBoneKey = new NamespacedKey(this, "dragon_bone");
        dashSwordKey = new NamespacedKey(this, "dash_sword");
        wardenTearKey = new NamespacedKey(this, "warden_tear");
        shockwingKey = new NamespacedKey(this, "shockwing");
        getawayCompassKey = new NamespacedKey(this, "getaway_compass");


        // Register commands
        getCommand("bedrockbreaker").setExecutor(new BedrockBreakerCommand(this));
        getCommand("axe30").setExecutor(new Axe30Command(this));
        getCommand("lootbundle").setExecutor(new LootBundleCommand());
        getCommand("getblade").setExecutor(new BloodthirsterCommand());
        getCommand("carrotcrossbow").setExecutor(new CarrotCrossbowCommand());
        getCommand("bunnysword").setExecutor(new BunnySwordCommand());
        getCommand("chorussword").setExecutor(new ChorusSwordCommand());
        getCommand("getcoffee").setExecutor(new CoffeeCommand());
        getCommand("crownofanarchy").setExecutor(new CrownOfAnarchyCommand());
        getCommand("getcookie").setExecutor(new GrandmasCookieCommand());
        getCommand("rubikscube").setExecutor(new RubiksCubeCommand());
        getCommand("cubanskull").setExecutor(new CubanSkullCommand());
        getCommand("dynamite").setExecutor(new DynamiteCommand());
        getCommand("cupidsbow").setExecutor(new CupidsBowCommand());
        getCommand("explosivecharge").setExecutor(new ExplosiveChargeCommand());
        getCommand("dragonfirework").setExecutor(new DragonFireworkCommand());
        getCommand("grapplinghook").setExecutor(new GrapplingHookCommand());
        getCommand("flyrod").setExecutor(new FlyRodCommand());
        getCommand("parawan").setExecutor(new ParawanCommand());
        getCommand("gravityaxe").setExecutor(new GravityAxeCommand());
        getCommand("poisonpencil").setExecutor(new PoisonPencilCommand());
        getCommand("areaminerpickaxe").setExecutor(new AreaMinerCommand());
        getCommand("trollsrose").setExecutor(new TrollsRoseCommand(trollsRoseKey));
        getCommand("pumpkinsword").setExecutor(new PumpkinSwordCommand(pumpkinSwordKey));
        getCommand("swapball").setExecutor(new SwapBallCommand());
        getServer().getPluginManager().registerEvents(new SwapBallListener(), this);
        getCommand("balloon").setExecutor(new BalloonCommand());
        getServer().getPluginManager().registerEvents(new BalloonListener(), this);

        getCommand("healingbeetrootsoup").setExecutor(new HealingBeetrootSoupCommand());
        getServer().getPluginManager().registerEvents(new HealingBeetrootSoupListener(), this);
        getCommand("totemofpardon").setExecutor(new TotemOfPardonCommand());
        getServer().getPluginManager().registerEvents(new TotemOfPardonListener(), this);

        getCommand("gettempest").setExecutor(new TempestHammerCommand());
        getServer().getPluginManager().registerEvents(new TempestHammerListener(), this);

        getCommand("getwall").setExecutor(new UnbreakableWallCommand());
        getServer().getPluginManager().registerEvents(new UnbreakableWallListener(), this);

        getCommand("warmmilk").setExecutor(new WarmMilkCommand());
        getServer().getPluginManager().registerEvents(new WarmMilkListener(), this);

        getCommand("blindscythe").setExecutor(new ScytheCommand());
        getServer().getPluginManager().registerEvents(new ScytheListener(), this);

        getCommand("dragonbone").setExecutor(new DragonBoneCommand());
        getServer().getPluginManager().registerEvents(new DragonBoneListener(), this);
        getCommand("earthmace").setExecutor(new MaceCommand(this));
        getServer().getPluginManager().registerEvents(new MaceListener(this), this);

        getCommand("withershot").setExecutor(new WitherSkullCommand());
        getServer().getPluginManager().registerEvents(new WitherSkullListener(), this);
        getCommand("dashsword").setExecutor(new DashSwordCommand(this));
        getServer().getPluginManager().registerEvents(new DashSwordListener(this), this);
        getCommand("voidsceptre").setExecutor(new VoidSceptreCommand());
        getServer().getPluginManager().registerEvents(new VoidSceptreListener(), this);
        getCommand("wardentear").setExecutor(new WardenTearCommand());
        getServer().getPluginManager().registerEvents(new WardenTearListener(), this);
        getCommand("shockwing").setExecutor(new ShockwingCommand());
        getServer().getPluginManager().registerEvents(new ShockwingListener(), this);
        getCommand("getaway").setExecutor(new GetAwayCommand());
        getServer().getPluginManager().registerEvents(new GetAwayListener(), this);
        // Register listeners
        getServer().getPluginManager().registerEvents(new BedrockBreakerListener(this), this);
        getServer().getPluginManager().registerEvents(new Axe30Listener(this), this);
        getServer().getPluginManager().registerEvents(new LootBundleListener(), this);
        getServer().getPluginManager().registerEvents(new BloodthirsterListener(), this);
        getServer().getPluginManager().registerEvents(new CarrotCrossbowListener(), this);
        getServer().getPluginManager().registerEvents(new BunnySwordListener(), this);
        getServer().getPluginManager().registerEvents(new ChorusSwordListener(), this);
        getServer().getPluginManager().registerEvents(new CoffeeListener(), this);
        getServer().getPluginManager().registerEvents(new CrownOfAnarchyListener(), this);
        getServer().getPluginManager().registerEvents(new GrandmasCookieListener(), this);
        getServer().getPluginManager().registerEvents(new RubiksCubeListener(), this);
        getServer().getPluginManager().registerEvents(new CubanSkullListener(), this);
        getServer().getPluginManager().registerEvents(new DynamiteListener(), this);
        getServer().getPluginManager().registerEvents(new CupidsBowListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosiveChargeListener(), this);
        getServer().getPluginManager().registerEvents(new DragonFireworkListener(), this);
        getServer().getPluginManager().registerEvents(new GrapplingHookListener(), this);
        getServer().getPluginManager().registerEvents(new FlyRodListener(), this);
        getServer().getPluginManager().registerEvents(new ParawanListener(), this);
        getServer().getPluginManager().registerEvents(new GravityAxeListener(), this);
        getServer().getPluginManager().registerEvents(new PoisonPencilListener(), this);
        getServer().getPluginManager().registerEvents(new AreaMinerListener(), this);
        getServer().getPluginManager().registerEvents(new TrollsRoseListener(trollsRoseKey), this);
        getServer().getPluginManager().registerEvents(new PumpkinSwordListener(pumpkinSwordKey), this);
        getServer().getPluginManager().registerEvents(new ScytheListener(), this);
        getServer().getPluginManager().registerEvents(new BalloonListener(), this);
        getServer().getPluginManager().registerEvents(new WitherSkullListener(), this);
        getServer().getPluginManager().registerEvents(new DragonBoneListener(), this);
    }
    @Override
    public void onDisable() {
        // Any shutdown logic if needed
    }

    // Getters for NamespacedKeys
    public NamespacedKey getTrollsRoseKey() { return trollsRoseKey; }
    public NamespacedKey getPumpkinSwordKey() { return pumpkinSwordKey; }
    public NamespacedKey getBedrockBreakerKey() { return bedrockBreakerKey; }
    public NamespacedKey getAxe30Key() { return axe30Key; }
    public NamespacedKey getLootBundleKey() { return lootBundleKey; }
    public NamespacedKey getBloodthirsterKey() { return bloodthirsterKey; }
    public NamespacedKey getCarrotCrossbowKey() { return carrotCrossbowKey; }
    public NamespacedKey getBunnySwordKey() { return bunnySwordKey; }
    public NamespacedKey getChorusSwordKey() { return chorusSwordKey; }
    public NamespacedKey getCoffeeKey() { return coffeeKey; }
    public NamespacedKey getCrownOfAnarchyKey() { return crownOfAnarchyKey; }
    public NamespacedKey getGrandmasCookieKey() { return grandmasCookieKey; }
    public NamespacedKey getRubiksCubeKey() { return rubiksCubeKey; }
    public NamespacedKey getCubanSkullKey() { return cubanSkullKey; }
    public NamespacedKey getDynamiteKey() { return dynamiteKey; }
    public NamespacedKey getCupidsBowKey() { return cupidsBowKey; }
    public NamespacedKey getExplosiveChargeKey() { return explosiveChargeKey; }
    public NamespacedKey getDragonFireworkKey() { return dragonFireworkKey; }
    public NamespacedKey getGrapplingHookKey() { return grapplingHookKey; }
    public NamespacedKey getFlyRodKey() { return flyRodKey; }
    public NamespacedKey getParawanKey() { return parawanKey; }
    public NamespacedKey getGravityAxeKey() { return gravityAxeKey; }
    public NamespacedKey getPoisonPencilKey() { return poisonPencilKey; }
    public NamespacedKey getAreaMinerKey() { return areaMinerKey; }
    public NamespacedKey getHealingBeetrootSoupKey() { return healingBeetrootSoupKey; }
    public NamespacedKey getTotemOfPardonKey() { return totemOfPardonKey; }
    public NamespacedKey getTempestHammerKey() { return tempestHammerKey; }
    public NamespacedKey getUnbreakableWallKey() { return unbreakableWallKey; }
    public NamespacedKey getWarmMilkKey() { return warmMilkKey; }
    public NamespacedKey getSwapBallKey() { return swapBallKey; }
    public NamespacedKey getScytheKey() { return ScytheKey; }
    public NamespacedKey getBalloonKey() { return BalloonKey; }
    public NamespacedKey getWitherSkullKey() { return WitherSkullKey; }
    public NamespacedKey getDragonBoneKey() { return DragonBoneKey; }
    public NamespacedKey getDashSwordKey() { return dashSwordKey; }
    public NamespacedKey getWardenTearKey() { return wardenTearKey; }
    public NamespacedKey getShockwingKey() { return shockwingKey; }
    public NamespacedKey getGetawayCompassKey() { return getawayCompassKey; }
}
