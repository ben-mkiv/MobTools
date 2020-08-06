package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.items.MobCollector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class MobCollectorItemInventory extends MobCollectorInventory {
    public ItemStack itemStack;

    public MobCollectorItemInventory(ItemStack stack){
        super(1);

        if(stack.getItem() instanceof MobCollector) {
            CompoundNBT nbt = stack.getOrCreateChildTag(MobTools.MOD_ID);

            if (!nbt.contains("inventory"))
                nbt.put("inventory", serializeNBT());

            deserializeNBT(nbt.getCompound("inventory"));
        }

        itemStack = stack;
    }

    @Override
    public void onContentsChanged(int slot){
        CompoundNBT nbt = itemStack.getOrCreateChildTag(MobTools.MOD_ID);

        nbt.put("inventory", serializeNBT());
    }
}
