package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MobSpawnerItem extends BlockItem {
    public static MobSpawnerItem DEFAULT;

    public MobSpawnerItem(){
        super(MobSpawnerBlock.DEFAULT, new Item.Properties().group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "mobspawner");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(new StringTextComponent("§7spawns mobs above it"));
        tooltip.add(new StringTextComponent("§7requires mob cartridge in it's inventory"));
        if(MobTools.useEnergy)
            tooltip.add(new StringTextComponent("§7requires "+MobTools.energyBaseCost+"FE * mobHealth to spawn a mob"));

        tooltip.add(new StringTextComponent("§7color indicates good placement position"));
        tooltip.add(new StringTextComponent("§7place in chunk center for §2best §7result"));
    }

    public static class LocationProperty implements IItemPropertyGetter {
        @Override
        public float call(ItemStack stack, ClientWorld world, LivingEntity entity) {
            // dont evaluate placement if the maximal radius isn't limited to half chunk size
            if(MobTools.spawnerMaxRadius > 7)
                return 0;

            // only change color when this method is called from a player entity
            if(entity == null || !(stack.getItem() instanceof MobSpawnerItem) || !entity.equals(Minecraft.getInstance().player))
                return 0;

            // only change color when the item is held by the player
            if(!Minecraft.getInstance().player.getHeldItemMainhand().equals(stack) && !Minecraft.getInstance().player.getHeldItemOffhand().equals(stack))
                return 0;

            // only change color when player has focus on a block
            if(!Minecraft.getInstance().objectMouseOver.getType().equals(RayTraceResult.Type.BLOCK))
                return 0;

            // get the position where the block *would* be placed if the player places it now
            BlockRayTraceResult result = ((BlockRayTraceResult) Minecraft.getInstance().objectMouseOver);
            BlockPos placePosition = result.getPos().add(result.getFace().getDirectionVec());

            int radius = maxChunkRadius(placePosition);

            if(radius < MobTools.spawnerMaxRadius)
                return 1;

            if(radius <= MobTools.spawnerMaxRadius)
                return 2;

            return 3;
        }
    }

    public static int maxChunkRadius(BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();

        x = Math.abs(x < 0 ? x+1 : x) % 16;
        z = Math.abs(z < 0 ? z+1 : z) % 16;

        return Math.min(Math.min(15-x, x), Math.min(15-z, z));
    }

}
