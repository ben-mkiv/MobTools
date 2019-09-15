package ben_mkiv.betterdispenser.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class EntityInventoryUtils {
    public static boolean consumeItemFromInventory(IInventory inventory, Item item, int amount){
        for(int slot=0; slot < inventory.getSizeInventory(); slot++){
            if(inventory.getStackInSlot(slot).getItem().equals(item)) {
                ItemStack stack = inventory.getStackInSlot(slot);
                int consumeAmount = Math.min(stack.getCount(), amount);
                stack.shrink(consumeAmount);
                amount-=consumeAmount;
                inventory.setInventorySlotContents(slot, stack);
            }
        }

        return amount == 0;
    }


    public static boolean consumeItemFromInventory(Entity entity, Item item, int amount){
        if(entity instanceof PlayerEntity){
            return EntityInventoryUtils.consumeItemFromInventory(((PlayerEntity) entity).inventory, item, amount);
        }

        return false;
    }

}
