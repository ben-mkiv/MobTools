package ben_mkiv.mobtools.inventory.container;

import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.energy.CustomEnergyStorage;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MobSpawnerContainer extends CustomContainer {
    public static final int width = 175, height = 195;

    public static ContainerType containerType;

    public MobSpawnerTileEntity spawner;


    public IntReferenceHolder energyStored = new IntReferenceHolder() {
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
    };

    public MobSpawnerContainer(PlayerEntity player, PlayerInventory inventoryPlayer, MobSpawnerTileEntity tile){
        super(containerType, MobSpawnerBlock.GUI_ID);

        spawner = tile;

        if(spawner != null) {
            addSlot(new SlotItemHandler(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 0, width / 2 - 18 / 2, 34));

            trackInt(energyStored);
        }

        bindPlayerInventory(inventoryPlayer, 8, 114);
    }

    public static MobSpawnerContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData){
        MobSpawnerTileEntity tile = (MobSpawnerTileEntity) Minecraft.getInstance().player.getEntityWorld().getTileEntity(extraData.readBlockPos());

        return new MobSpawnerContainer(Minecraft.getInstance().player, playerInventory, tile);
    }

}
