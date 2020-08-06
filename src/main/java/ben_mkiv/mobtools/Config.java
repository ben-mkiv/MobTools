package ben_mkiv.mobtools;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> totalMobCap, spawnerMaxRadius, energyBaseCost;

        public final ForgeConfigSpec.ConfigValue<Boolean> verboseDebug, useEnergy, allowBossCapture, allowBossSpawn;


        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            totalMobCap = builder
                    .comment("maximum amount of all mobs")
                    .translation("config.totalmobcap")
                    .define("totalMobCap", 50);

            spawnerMaxRadius = builder
                    .comment("maximum allowed radius for the spawner")
                    .translation("config.spawnermaxradius")
                    .define("spawnerMaxRadius", 5);

            energyBaseCost = builder
                    .comment("energy base cost, final cost is baseCost * mobHealth")
                    .translation("config.energybasecost")
                    .define("energyBaseCost", 10);

            verboseDebug = builder
                    .comment("output debug messages to logfile")
                    .translation("config.verbosedebug")
                    .define("verboseDebug", false);

            useEnergy = builder
                    .comment("spawner requires forge energy")
                    .translation("config.useenergy")
                    .define("useEnergy", true);

            allowBossCapture = builder
                    .comment("allow boss mobs to be captured")
                    .translation("config.allowbosscapture")
                    .define("allowBossCapture", false);

            allowBossSpawn = builder
                    .comment("allow boss mobs to be spawned")
                    .translation("config.allowbossspawn")
                    .define("allowBossSpawn", false);

            builder.pop();
        }
    }
}