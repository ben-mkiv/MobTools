package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

public class MobCartridge extends Item {
    public static MobCartridge DEFAULT;

    public MobCartridge(){
        super(new Properties().maxStackSize(1).group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "mobcartridge");
    }

    public static CompoundNBT getNBT(ItemStack cartridge){
        CompoundNBT nbt = cartridge.getOrCreateChildTag(MobTools.MOD_ID);

        if(!nbt.contains("entities", 10)) {
            nbt.put("entities", new CompoundNBT());
            nbt.getCompound("entities").putInt("index", 0);
        }

        return nbt;
    }

    public static boolean canStoreMob(ItemStack cartridge, Entity entity){
        return cartridge.getItem() instanceof MobCartridge;
    }

    public static boolean storeMob(ItemStack cartridge, Entity entity){
        if(!canStoreMob(cartridge, entity))
            return false;

        CompoundNBT tag = new CompoundNBT();
        entity.writeUnlessRemoved(tag);

        CompoundNBT nbt = getNBT(cartridge);

        int currentIndex = nbt.getCompound("entities").getInt("index");

        nbt.getCompound("entities").put("entity" + currentIndex, tag);

        nbt.getCompound("entities").putInt("index", (currentIndex+1));

        return true;
    }

    public static CompoundNBT extractMob(ItemStack cartridge){
        CompoundNBT nbt = getNBT(cartridge);

        int currentIndex = nbt.getCompound("entities").getInt("index");

        return extractMob(cartridge, (currentIndex-1));
    }

    public static CompoundNBT extractMob(ItemStack cartridge, int index){
        ArrayList<CompoundNBT> list = getStoredEntities(cartridge);

        if(index < 0 || list.size() < index)
            return null;

        CompoundNBT entityNBT = list.remove(index);

        CompoundNBT nbt = getNBT(cartridge);

        CompoundNBT entitiesNBT = new CompoundNBT();

        entitiesNBT.putInt("index", list.size());

        for(int i=0; i < list.size(); i++){
            entitiesNBT.put("entity"+i, list.get(i));
        }

        nbt.put("entities", entitiesNBT);

        return entityNBT;
    }

    public static ArrayList<CompoundNBT> getStoredEntities(ItemStack cartridge){
        ArrayList<CompoundNBT> list = new ArrayList<>();

        if(cartridge.getItem() instanceof MobCartridge) {
            CompoundNBT nbt = getNBT(cartridge);

            for (int i = 0; i < nbt.getCompound("entities").getInt("index"); i++) {
                if (nbt.getCompound("entities").contains("entity" + i))
                    list.add(nbt.getCompound("entities").getCompound("entity" + i));
            }
        }

        return list;
    }

}
