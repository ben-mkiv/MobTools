package ben_mkiv.betterdispenser;

import ben_mkiv.betterdispenser.capability.DispenserCapability;
import ben_mkiv.betterdispenser.capability.IDispenserCapability;
import ben_mkiv.betterdispenser.capability.capability;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(modid = BetterDispenser.MOD_ID)
@Mod(value = BetterDispenser.MOD_ID)
public class BetterDispenser {

    public static final String MOD_ID = "betterdispenser";
    public static final String MOD_NAME = "BetterDispenser";
    public static final String VERSION = "1.1";

    public static boolean verbose = false;


    public BetterDispenser(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(
                (FMLCommonSetupEvent event) -> BetterDispenser.registerCapability()
        );

        modEventBus.addListener(
                (FMLLoadCompleteEvent event) -> DispenserCapability.initConfig()
        );

        modEventBus.addListener(
                (FMLLoadCompleteEvent event) -> BetterDispenser.initConfig()
        );
    }

    private static void registerCapability(){
        CapabilityManager.INSTANCE.register(IDispenserCapability.class, new capability.Storage(), new capability.Factory());
    }

    private static void initConfig(){
        verbose = Config.GENERAL.verboseDebug.get();
    }

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent event) {
        if(!(event.getObject() instanceof DispenserTileEntity))
            return;

        DispenserTileEntity dispenser = (DispenserTileEntity) event.getObject();

        if(!dispenser.getCapability(capability.CAPABILITY).isPresent()) {
            event.addCapability(capability.DISPENSER_CAPABILITY, new capability(dispenser));
        }
    }


    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickBlock event) {
        if(event.getWorld().isRemote)
            return;

        if(!DispenserCapability.getInteractionItems().contains(event.getItemStack().getItem()))
            return;

        TileEntity tile = event.getWorld().getTileEntity(event.getPos());

        if(tile instanceof DispenserTileEntity){
            tile.getCapability(capability.CAPABILITY).ifPresent(foo -> foo.playerInteract(event));
        }
    }


}
