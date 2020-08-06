package ben_mkiv.betterdispenser.items;

import ben_mkiv.betterdispenser.MobTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MobCollector extends Item {

    static Item.Properties properties;

    static {
        properties.maxStackSize(1);
    }

    public MobCollector(){
        super(properties);
        setRegistryName(MobTools.MOD_ID, "mobcollector");
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if(player.isSneaking()){
            openInventory(player.inventory.getCurrentItem(), player);
            return ActionResult.resultSuccess(player.inventory.getCurrentItem());
        }

        return super.onItemRightClick(world, player, hand);
    }

    public static void openInventory(ItemStack stack, PlayerEntity player){
        System.out.println("open inventory");
    }
}
