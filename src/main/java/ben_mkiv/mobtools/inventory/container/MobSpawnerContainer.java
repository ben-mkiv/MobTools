package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MobSpawnerContainer extends CustomContainer {
    public static final int width = 175, height = 195;

    public static ContainerType containerType;

    public MobSpawnerTileEntity spawner;

    public MobSpawnerContainer(PlayerEntity player, PlayerInventory inventoryPlayer, MobSpawnerTileEntity tile){
        super(containerType, MobSpawnerBlock.GUI_ID);

        spawner = tile;

        if(spawner != null) {
            addSlot(new SlotItemHandler(spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null), 0, width / 2 - 18 / 2, 34));
        }

        bindPlayerInventory(inventoryPlayer, 8, 114);
    }

    public static MobSpawnerContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData){
        MobSpawnerTileEntity tile = (MobSpawnerTileEntity) Minecraft.getInstance().player.getEntityWorld().getTileEntity(extraData.readBlockPos());

        return new MobSpawnerContainer(Minecraft.getInstance().player, playerInventory, tile);
    }

}
