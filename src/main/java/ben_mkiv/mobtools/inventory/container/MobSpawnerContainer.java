package ben_mkiv.mobtools.inventory.container;

import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.client.gui.IContainerCallback;
import ben_mkiv.mobtools.client.gui.IntReferenceHolderSmart;
import ben_mkiv.mobtools.energy.CustomEnergyStorage;
import ben_mkiv.mobtools.inventory.slots.SpecialItemSlot;
import ben_mkiv.mobtools.items.MobCollector;
import ben_mkiv.mobtools.items.UpgradeRangeItem;
import ben_mkiv.mobtools.items.UpgradeSpeedItem;
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

    public IContainerCallback callback;

    public static ContainerType containerType;

    public MobSpawnerTileEntity spawner;

    private static HashSet<Item> cartridgeSlotItems = new HashSet<>();
    private static HashSet<Item> upgradeRangeSlotItems = new HashSet<>();
    private static HashSet<Item> upgradeSpeedSlotItems = new HashSet<>();

    public MobSpawnerContainer(PlayerInventory inventoryPlayer, MobSpawnerTileEntity tile){
        super(containerType, MobSpawnerBlock.GUI_ID);

        if(cartridgeSlotItems.isEmpty()){
            cartridgeSlotItems.add(MobCollector.DEFAULT);
        }
        if(upgradeRangeSlotItems.isEmpty()){
            upgradeRangeSlotItems.add(UpgradeRangeItem.DEFAULT);
        }
        if(upgradeSpeedSlotItems.isEmpty()){
            upgradeSpeedSlotItems.add(UpgradeSpeedItem.DEFAULT);
        }

        if(tile != null) {
            spawner = tile;


            addSlot(new SpecialItemSlot(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 1, width - 24 , 38, upgradeRangeSlotItems));
            addSlot(new SpecialItemSlot(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 2, width - 24 , 61, upgradeSpeedSlotItems));

            addSlot(new SpecialItemSlot(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 0, width - 24 , 84, cartridgeSlotItems));



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

        // energy lower bits
        trackInt(new IntReferenceHolderSmart() {
                @Override
                public int get() {
                    IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                    return energyStorage != null ? (energyStorage.getEnergyStored() & 0xFFFF) : 0;
                }

                @Override
                public void set(int energy) {
                    IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                    if(energyStorage instanceof CustomEnergyStorage) {
                        int val = energyStorage.getEnergyStored();
                        val&= 0xFFFF << 16;
                        val+=energy;
                        ((CustomEnergyStorage) energyStorage).setEnergyStored(val);
                    }
                }
            });

        // energy higher bits
        trackInt(new IntReferenceHolderSmart() {
            @Override
            public int get() {
                IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                return energyStorage != null ? (energyStorage.getEnergyStored() >> 16) : 0;
            }

            @Override
            public void set(int energy) {
                IEnergyStorage energyStorage = spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                if(energyStorage instanceof CustomEnergyStorage) {
                    int val = energyStorage.getEnergyStored();
                    val&= 0xFFFF;
                    val+= (energy << 16);
                    ((CustomEnergyStorage) energyStorage).setEnergyStored(val);
                }
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

        // upgrades
        trackInt(new IntReferenceHolderSmart() {
            @Override
            public int get() {
                int value = 0;
                if(spawner.upgradeSpeed) value|=1 << 1;
                if(spawner.upgradeRange) value|=1 << 2;
                return value;
            }

            @Override
            public void set(int value) {
                spawner.upgradeSpeed = (value & 1 << 1) != 0;
                spawner.upgradeRange = (value & 1 << 2) != 0;

                if(callback != null)
                    callback.containerCallback();
            }
        });
    }

}

