package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.items.UpgradeRangeItem;
import ben_mkiv.mobtools.items.UpgradeSpeedItem;
import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class MobSpawnerInventory extends MobCollectorInventory {

    MobSpawnerTileEntity tile;

    public MobSpawnerInventory(MobSpawnerTileEntity spawner){
        super(3);
        tile = spawner;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        switch(slot) {
            case 1: return stack.getItem() instanceof UpgradeRangeItem;
            case 2: return stack.getItem() instanceof UpgradeSpeedItem;
            default: return super.isItemValid(slot, stack);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        if(tile != null) {
            tile.reloadInventory(slot);
        }

        super.onContentsChanged(slot);
    }


}
