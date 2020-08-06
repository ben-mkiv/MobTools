package ben_mkiv.mobtools.inventory.container;

import ben_mkiv.mobtools.inventory.MobCollectorItemInventory;
import ben_mkiv.mobtools.inventory.slots.SpecialItemSlot;
import ben_mkiv.mobtools.items.MobCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.HashSet;


public class MobCollectorContainer extends CustomContainer {
    public static final int width = 175, height = 195;

    public static ContainerType containerType;

    static HashSet<Item> cartridgeSlotItems = new HashSet<>();


    public ItemStack itemStack;
    public IItemHandler inventory;


    public MobCollectorContainer(PlayerEntity player, PlayerInventory inventoryPlayer, ItemStack mobCollectorStack){
        super(containerType, MobCollector.GUI_ID);

        if(cartridgeSlotItems.isEmpty()){
            cartridgeSlotItems.add(MobCollector.DEFAULT);
        }


        itemStack = mobCollectorStack;

        inventory = new MobCollectorItemInventory(itemStack);


        addSlot(new SpecialItemSlot(inventory, 0, width - 27 , 81, cartridgeSlotItems));


        bindPlayerInventory(inventoryPlayer, 8, 114);
    }

    public static MobCollectorContainer createContainerClientSide(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData){
        ItemStack stack = extraData.readItemStack();
        return new MobCollectorContainer(Minecraft.getInstance().player, playerInventory, stack);
    }



}
