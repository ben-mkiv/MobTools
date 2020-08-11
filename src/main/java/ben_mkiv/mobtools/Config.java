package ben_mkiv.mobtools;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> totalMobCap, spawnerMaxRadius, energyBaseCost, spawnerMinTickDelay;

        public final ForgeConfigSpec.ConfigValue<Boolean> verboseDebug, useEnergy, allowBossCapture, allowBossSpawn, badPlacementPenalty;


        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            totalMobCap = builder
                    .comment("maximum amount of all mobs")
                    .translation("config.totalMobCap")
                    .define("totalMobCap", 10);

            spawnerMaxRadius = builder
                    .comment("maximum allowed radius for the spawner")
                    .translation("config.spawnerMaxRadius")
                    .define("spawnerMaxRadius", 6);

            spawnerMinTickDelay = builder
                    .comment("minimum tick delay for mobSpawner")
                    .translation("config.spawnerMinTickDelay")
                    .define("spawnerMinTickDelay", 50);

            energyBaseCost = builder
                    .comment("energy base cost, final cost is baseCost * mobHealth")
                    .translation("config.energyBaseCost")
                    .define("energyBaseCost", 10);

            verboseDebug = builder
                    .comment("output debug messages to logfile")
                    .translation("config.verboseDebug")
                    .define("verboseDebug", false);

            useEnergy = builder
                    .comment("spawner requires forge energy")
                    .translation("config.useEnergy")
                    .define("useEnergy", true);

            allowBossCapture = builder
                    .comment("allow boss mobs to be captured")
                    .translation("config.allowBossCapture")
                    .define("allowBossCapture", false);

            allowBossSpawn = builder
                    .comment("allow boss mobs to be spawned")
                    .translation("config.allowBossSpawn")
                    .define("allowBossSpawn", false);

            badPlacementPenalty = builder
                    .comment("limit range of spawner to the chunk it's placed in")
                    .translation("config.badPlacementPenalty")
                    .define("badPlacementPenalty", true);

            builder.pop();
        }
    }
}