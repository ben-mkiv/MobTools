package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.items.MobCartridge;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class MobCollectorInventory extends ItemStackHandler {
    MobSpawnerTileEntity tile;

    public MobCollectorInventory(int size){
        super(size);
    }

    public MobCollectorInventory(int size, MobSpawnerTileEntity spawner){
        this(size);
        tile = spawner;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof MobCartridge;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if(tile != null) {
            tile.reloadInventory();
        }

        super.onContentsChanged(slot);
    }
}
