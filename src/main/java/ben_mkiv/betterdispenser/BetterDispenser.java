package ben_mkiv.betterdispenser;

import ben_mkiv.betterdispenser.capability.DispenserCapability;
import ben_mkiv.betterdispenser.capability.IDispenserCapability;
import ben_mkiv.betterdispenser.capability.capability;
import net.minecraft.item.Items;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
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


    public BetterDispenser(){
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.spec);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(
                (FMLCommonSetupEvent event) -> preInit()
        );

        modEventBus.addListener(
                (FMLLoadCompleteEvent event) -> DispenserCapability.initConfig()
        );
    }

    private void preInit(){
        CapabilityManager.INSTANCE.register(IDispenserCapability.class, new capability.Storage(), new capability.Factory());
    }

    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent event) {
        if(!(event.getObject() instanceof DispenserTileEntity))
            return;

        DispenserTileEntity dispenser = (DispenserTileEntity) event.getObject();

        if(dispenser.getCapability(capability.CAPABILITY, null).equals(LazyOptional.empty())) {
            event.addCapability(capability.DISPENSER_CAPABILITY, new capability(dispenser));
        }
    }


    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent event) {
        if(event.getWorld().isRemote)
            return;

        if(!event.getItemStack().getItem().equals(Items.BLAZE_ROD))
            return;

        TileEntity tile = event.getWorld().getTileEntity(event.getPos());

        if(tile instanceof DispenserTileEntity){
            LazyOptional<IDispenserCapability> cap = tile.getCapability(capability.CAPABILITY, null);
            cap.ifPresent(foo -> foo.playerInteract(event.getPlayer()));
            event.setCanceled(true);
        }
    }


}
