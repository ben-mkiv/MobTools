package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import net.minecraft.item.Item;

public class UpgradeRangeItem extends Item {
    public static UpgradeRangeItem DEFAULT;

    public UpgradeRangeItem(){
        super(new Item.Properties().group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "rangeupgrade");
    }
}

