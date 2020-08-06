package ben_mkiv.mobtools.utils;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class ItemUtils {

    public static void dropInventory(World world, ItemStackHandler inventory, BlockPos pos, boolean motion, int pickupDelay) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropItem(inventory.getStackInSlot(slot), world, pos, motion, pickupDelay);
        }
    }


    public static void dropItem(ItemStack stack, World world, BlockPos pos, boolean motion, int pickupDelay) {
        if (world.isRemote) return;

        if (stack.getMaxStackSize() <= 0 || stack.isEmpty())
            return;

        ItemEntity entityitem = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        entityitem.setPickupDelay(pickupDelay);
        if (!motion) {
            entityitem.setMotion(0, 0, 0);
        }

        world.addEntity(entityitem);
    }

}