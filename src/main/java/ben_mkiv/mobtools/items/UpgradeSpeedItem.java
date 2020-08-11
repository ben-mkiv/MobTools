package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import net.minecraft.item.Item;

public class UpgradeSpeedItem extends Item {
    public static UpgradeSpeedItem DEFAULT;

    public UpgradeSpeedItem(){
        super(new Item.Properties().group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "speedupgrade");
    }
}
