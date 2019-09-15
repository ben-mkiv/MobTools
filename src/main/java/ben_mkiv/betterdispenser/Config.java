package ben_mkiv.betterdispenser;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General GENERAL = new General(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Integer> mobCap;
        public final ForgeConfigSpec.ConfigValue<Integer> totalMobCap;
        public final ForgeConfigSpec.ConfigValue<Integer> maxRadius;
        public final ForgeConfigSpec.ConfigValue<Integer> breedingTimeout;

        public General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            mobCap = builder
                    .comment("maximum amount of mobs of the same type")
                    .translation("config.mobcap")
                    .define("mobCap", 25);

            totalMobCap = builder
                    .comment("maximum amount of all mobs")
                    .translation("config.totalmobcap")
                    .define("totalMobCap", 50);

            maxRadius = builder
                    .comment("maximum breeding radius")
                    .translation("config.radiuslimit")
                    .define("radiusLimit", 10);

            breedingTimeout = builder
                    .comment("breeding timeout in ticks (20 ticks approx. 1 second)")
                    .translation("config.breedingtimeout")
                    .define("breedingTimeout", 600);

            builder.pop();
        }
    }
}