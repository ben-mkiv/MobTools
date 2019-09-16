package ben_mkiv.betterdispenser.capability;

import ben_mkiv.betterdispenser.Config;
import ben_mkiv.betterdispenser.utils.EntityInventoryUtils;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DispenserCapability implements IDispenserCapability {
    private DispenserTileEntity dispenser = null;

    static int mobCap = 25;
    static int totalMobCap = 50;
    static int maxRadius = 10;
    static int timeout = 200;

    private int radius = 3;
    private boolean active = false;

    HashMap<Class, EntityCouple> breeding = new HashMap<>();

    HashSet<ItemStack> inventoryItems = new HashSet<>();

    DispenserCapability(DispenserTileEntity dispenserTileEntity){
        dispenser = dispenserTileEntity;
        updateEventHandler();
    }

    public static void initConfig(){
        mobCap = Config.GENERAL.mobCap.get();
        totalMobCap = Config.GENERAL.totalMobCap.get();
        maxRadius = Config.GENERAL.maxRadius.get();
        timeout = Config.GENERAL.breedingTimeout.get();
    }

    private void updateEventHandler(){
        if(active) {
            MinecraftForge.EVENT_BUS.unregister(this);
            MinecraftForge.EVENT_BUS.register(this);
        }
        else
            MinecraftForge.EVENT_BUS.unregister(this);
    }

    int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent event){
        if(!active)
            return;

        if(ticks++ % 20 != 0)
            return;

        ticks = 0;

        if(!event.phase.equals(TickEvent.Phase.END))
            return;

        if(dispenser == null || dispenser.isRemoved()){
            MinecraftForge.EVENT_BUS.unregister(this);
            return;
        }

        if(!dispenser.getWorld().getDimension().getType().equals(event.world.getDimension().getType()))
            return;

        if(dispenser.getWorld().isRemote())
            MinecraftForge.EVENT_BUS.unregister(this);

        if(!dispenser.getWorld().isAreaLoaded(dispenser.getPos(), 1))
            return;

        //if(dispenser.getBlockState().getWeakPower(dispenser.getWorld(), dispenser.getPos(), Direction.DOWN) == 0)
        //    return;

        inventoryItems = getInventoryItems();

        AxisAlignedBB area = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(dispenser.getPos());

        List<AnimalEntity> entityList = dispenser.getWorld().getEntitiesWithinAABB(AnimalEntity.class, area);

        if(entityList.size() > totalMobCap)
            return;

        for(AnimalEntity entity : entityList){
            if(entity.isChild() || !entity.canBreed() || entity.getGrowingAge() != 0)
                continue;

            if(dispenser.getWorld().getEntitiesWithinAABB(entity.getClass(), area).size() > mobCap)
                continue;

            if(!breeding.containsKey(entity.getClass()))
                breeding.put(entity.getClass(), new EntityCouple());

            if(!breeding.get(entity.getClass()).canWork())
                continue;

            if(breeding.get(entity.getClass()).increase(entity))
                return;
        }
    }

    public void playerInteract(PlayerEntity player){
        if(player.isSneaking())
            changeRadius(player);
        else
            toggleMode(player);
    }

    public void changeRadius(PlayerEntity player){
        radius++;

        if(radius > maxRadius)
            radius = 1;

        player.sendStatusMessage(new StringTextComponent("radius changed to " + radius + " (limit: " + maxRadius + ")"), true);
    }

    public void toggleMode(PlayerEntity player){
        active = !active;

        if(active)
            player.sendStatusMessage(new StringTextComponent("breeding enabled"), true);
        else
            player.sendStatusMessage(new StringTextComponent("breeding disabled"), true);

        updateEventHandler();
    }


    private boolean feedEntity(AnimalEntity entity){
        for(ItemStack stack : inventoryItems) {
            if (entity.isBreedingItem(stack)) {
                EntityInventoryUtils.consumeItemFromInventory(dispenser, stack.getItem(), 1);
                entity.setInLove(null);

                System.out.println("fed " + entity.getClass().getSimpleName() + ", " + entity.getUniqueID());

                return true;
            }
        }

        return false;
    }

    private HashSet<ItemStack> getInventoryItems(){
        HashSet<ItemStack> items = new HashSet<>();
        for(int slot=0; slot < dispenser.getSizeInventory(); slot++)
            if(!dispenser.getStackInSlot(slot).isEmpty())
                items.add(dispenser.getStackInSlot(slot));

        return items;
    }

    class EntityCouple {
        long nextFeeding = System.currentTimeMillis();
        private HashSet<AnimalEntity> parents = new HashSet<>();


        public boolean increase(AnimalEntity entity){
            if(parents.contains(entity))
                return false;

            parents.add(entity);

            if(parents.size() >= 2) {
                nextFeeding = System.currentTimeMillis() + ((timeout/20) * 500);

                for(AnimalEntity parent : parents) {
                    if(parent == null || !parent.isAlive()) {
                        parents.remove(parent);
                        return false;
                    }

                    if (!feedEntity(parent))
                        return false;
                }

                parents.clear();

                return true;
            }
            else {
                nextFeeding = System.currentTimeMillis() + 5000;
                return false;
            }
        }

        public boolean canWork(){
            return nextFeeding <= System.currentTimeMillis();
        }
    }

    public CompoundNBT writeToNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("active", active);
        nbt.putInt("radius", radius);
        return nbt;
    }

    public void readFromNBT(CompoundNBT nbt){
        if(nbt.contains("active"))
            active = nbt.getBoolean("active");
        if(nbt.contains("radius"))
            radius = nbt.getInt("radius");

        updateEventHandler();
    }

}
