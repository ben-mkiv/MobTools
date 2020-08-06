package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.items.MobCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;


public class MobCollectorContainer extends CustomContainer {
    public static final int width = 175, height = 195;

    public static ContainerType containerType;


    public ItemStack itemStack;
    public IItemHandler inventory;


    public MobCollectorContainer(PlayerEntity player, PlayerInventory inventoryPlayer, ItemStack mobCollectorStack){
        super(containerType, MobCollector.GUI_ID);
        itemStack = mobCollectorStack;

        inventory = new MobCollectorItemInventory(itemStack);

        addSlot(new SlotItemHandler(inventory, 0, width/2 - 18/2, 34));


        bindPlayerInventory(inventoryPlayer, 8, 114);
    }

    public static MobCollectorContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData){
        ItemStack stack = extraData.readItemStack();
        return new MobCollectorContainer(Minecraft.getInstance().player, playerInventory, stack);
    }



}
