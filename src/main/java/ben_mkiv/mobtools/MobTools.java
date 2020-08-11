package ben_mkiv.mobtools;

import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.inventory.container.MobCollectorContainer;
import ben_mkiv.mobtools.client.gui.MobCollectorContainerScreen;
import ben_mkiv.mobtools.inventory.container.MobSpawnerContainer;
import ben_mkiv.mobtools.client.gui.MobSpawnerContainerScreen;
import ben_mkiv.mobtools.items.*;
import ben_mkiv.mobtools.network.NetworkPacketBase;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = MobTools.MOD_ID)
@Mod(value = MobTools.MOD_ID)
public class MobTools {

    public static final String MOD_ID = "mobtools";
    public static final String MOD_NAME = "MobTools";
    public static final String VERSION = "1.0";

    public static boolean verbose = false;
    public static boolean useEnergy = true;
    public static boolean allowBossCapture = false;
    public static boolean allowBossSpawn = false;
    public static int energyBaseCost = 10;
    public static int entityCountLimit = 10;
    public static int spawnerMaxRadius = 5;
    public static int spawnerMinTickDelay = 50;
    public static int spawnerMaxTickDelay = 200;
    public static boolean badPlacementPenalty = true;

    public static final ItemGroup CREATIVE_TAB = new ItemGroup(MOD_ID+".creativeTab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(MobCollector.DEFAULT);
        }
    };

    public MobTools(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(
                (FMLLoadCompleteEvent event) -> MobTools.initConfig()
        );

        modEventBus.addListener(
                (FMLCommonSetupEvent event) -> MobTools.initCommon()
        );
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(MobSpawnerBlock.DEFAULT = new MobSpawnerBlock());
        }

        @SubscribeEvent
        public static void onItemRegistry(RegistryEvent.Register<Item> event) {
            event.getRegistry().register(MobCollector.DEFAULT = new MobCollector());
            event.getRegistry().register(MobCartridge.DEFAULT = new MobCartridge());
            event.getRegistry().register(MobSpawnerItem.DEFAULT = new MobSpawnerItem());
            event.getRegistry().register(UpgradeSpeedItem.DEFAULT = new UpgradeSpeedItem());
            event.getRegistry().register(UpgradeRangeItem.DEFAULT = new UpgradeRangeItem());
        }

        @SubscribeEvent
        public static void onTileRegistryReady(RegistryEvent.Register<TileEntityType<?>> event) {
            MobSpawnerTileEntity.tileEntityType = TileEntityType.Builder.create(MobSpawnerTileEntity::new, MobSpawnerBlock.DEFAULT).build(null);
            MobSpawnerTileEntity.tileEntityType.setRegistryName(MOD_ID, "spawner_tileentity");
            event.getRegistry().register(MobSpawnerTileEntity.tileEntityType);
        }

        @SubscribeEvent
        public static void onContainerTypeRegistryReady(RegistryEvent.Register<ContainerType<?>> event) {
            MobCollectorContainer.containerType = IForgeContainerType.create(MobCollectorContainer::createContainerClientSide);
            MobCollectorContainer.containerType.setRegistryName("mobcollector_container");
            event.getRegistry().register(MobCollectorContainer.containerType);

            MobSpawnerContainer.containerType = IForgeContainerType.create(MobSpawnerContainer::createContainerClientSide);
            MobSpawnerContainer.containerType.setRegistryName("mobspawner_container");
            event.getRegistry().register(MobSpawnerContainer.containerType);
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            ScreenManager.registerFactory(MobCollectorContainer.containerType, MobCollectorContainerScreen::new);
            ScreenManager.registerFactory(MobSpawnerContainer.containerType, MobSpawnerContainerScreen::new);


            DeferredWorkQueue.runLater(new Runnable() {
                @Override
                public void run() {
                    ItemModelsProperties.func_239418_a_(MobSpawnerItem.DEFAULT, new ResourceLocation(MOD_ID, "location"), new MobSpawnerItem.LocationProperty());
                }
            });
        }
    }

    private static void initCommon(){
        NetworkPacketBase.init();
    }

    private static void initConfig(){
        verbose = Config.GENERAL.verboseDebug.get();

        allowBossCapture = Config.GENERAL.allowBossCapture.get();
        allowBossSpawn = Config.GENERAL.allowBossSpawn.get();

        energyBaseCost = Config.GENERAL.energyBaseCost.get();
        useEnergy = Config.GENERAL.useEnergy.get();

        spawnerMaxRadius = Config.GENERAL.spawnerMaxRadius.get();

        entityCountLimit = Config.GENERAL.totalMobCap.get();

        spawnerMinTickDelay = Config.GENERAL.spawnerMinTickDelay.get();

        badPlacementPenalty = Config.GENERAL.badPlacementPenalty.get();
    }

}
