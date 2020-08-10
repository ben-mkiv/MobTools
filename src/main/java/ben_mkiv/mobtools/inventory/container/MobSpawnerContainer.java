package ben_mkiv.mobtools.inventory.container;

import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.client.gui.IntReferenceHolderSmart;
import ben_mkiv.mobtools.energy.CustomEnergyStorage;
import ben_mkiv.mobtools.inventory.slots.SpecialItemSlot;
import ben_mkiv.mobtools.items.MobCollector;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import java.util.HashSet;

public class MobSpawnerContainer extends CustomContainer {
    public static final int width = 175, height = 195;

    public static ContainerType containerType;

    public MobSpawnerTileEntity spawner;

    private static HashSet<Item> cartridgeSlotItems = new HashSet<>();

    public MobSpawnerContainer(PlayerInventory inventoryPlayer, MobSpawnerTileEntity tile){
        super(containerType, MobSpawnerBlock.GUI_ID);

        if(cartridgeSlotItems.isEmpty()){
            cartridgeSlotItems.add(MobCollector.DEFAULT);
        }

        spawner = tile;

        if(spawner != null) {
            addSlot(new SpecialItemSlot(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 0, width - 27 , 81, cartridgeSlotItems));

            setupTrackingValues();
        }

        bindPlayerInventory(inventoryPlayer, 8, 114);
    }

    public static MobSpawnerContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData){
        MobSpawnerTileEntity tile = (MobSpawnerTileEntity) playerInventory.player.getEntityWorld().getTileEntity(extraData.readBlockPos());
        return new MobSpawnerContainer(playerInventory, tile);
    }


    /* watched values */
    private void setupTrackingValues(){

        // energy
        trackInt(new IntReferenceHolderSmart() {
                @Override
                public int get() {
                    IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                    return energyStorage != null ? energyStorage.getEnergyStored() : 0;
                }

                @Override
                public void set(int energy) {
                    IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                    if(energyStorage instanceof CustomEnergyStorage)
                        ((CustomEnergyStorage) energyStorage).setEnergyStored(energy);
                }
            });

        // radius
        trackInt(new IntReferenceHolderSmart() {
                @Override
                public int get() {
                    return spawner.radius;
                }

                @Override
                public void set(int value) {
                    spawner.radius = value;
                }
            });

        // redstone
        trackInt(new IntReferenceHolderSmart() {
                @Override
                public int get() {
                    return spawner.isRedstonePowered ? 1 : 0;
                }

                @Override
                public void set(int value) {
                    spawner.isRedstonePowered = value == 1;
                }
            });

        // tickDelay
        trackInt(new IntReferenceHolderSmart() {
                @Override
                public int get() {
                    return spawner.tickDelay;
                }

                @Override
                public void set(int value) {
                    spawner.tickDelay = value;
                }
            });
    }

}

