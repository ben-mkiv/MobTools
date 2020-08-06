package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MobSpawnerItem extends BlockItem {
    public static MobSpawnerItem DEFAULT;

    public MobSpawnerItem(){
        super(MobSpawnerBlock.DEFAULT, new Item.Properties().group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "mobspawner");
    }

    public static class LocationProperty implements IItemPropertyGetter {
        public float call(ItemStack stack, ClientWorld world, LivingEntity entity) {
            System.out.println("get property");
            return entity == null || !(stack.getItem() instanceof MobSpawnerItem) ? 0.0F : (entity.ticksExisted % 3);
        }
    }

}
