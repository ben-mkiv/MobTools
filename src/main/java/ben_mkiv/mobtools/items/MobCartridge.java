package ben_mkiv.betterdispenser.items;

import ben_mkiv.betterdispenser.MobTools;
import net.minecraft.item.Item;

public class MobCartridge extends Item {

    static Item.Properties properties;

    static {
        properties.maxStackSize(1);
    }

    public MobCartridge(){
        super(properties);
        setRegistryName(MobTools.MOD_ID, "mobcartridge");
    }


}
