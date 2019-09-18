package ben_mkiv.betterdispenser.capability;

import ben_mkiv.betterdispenser.BetterDispenser;
import ben_mkiv.betterdispenser.Config;
import ben_mkiv.betterdispenser.utils.EntityInventoryUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class DispenserCapability implements IDispenserCapability {
    static int mobCap = 25;
    static int totalMobCap = 50;
    static int maxRadius = 10;
    static int timeout = 200;

    static List<Item> interactionItems = new ArrayList<>();
    static HashMap<UUID, UserInputEventListener> chatListeners = new HashMap<>();

    static {
        interactionItems.add(Items.BLAZE_ROD);
        interactionItems.add(Items.NETHER_STAR);
    }


    private DispenserTileEntity dispenser = null;

    private int radius = 3;
    private boolean active = false;

    private HashMap<Class, EntityCouple> breeding = new HashMap<>();

    private HashSet<ItemStack> inventoryItems = new HashSet<>();

    private HashSet<Class> entityFilter = new HashSet<>();


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


        List<AnimalEntity> entityList = getAnimalsWithinRange();

        if(entityList.size() > totalMobCap)
            return;

        for(AnimalEntity entity : entityList){
            if(entityFilter.size() > 0 && !entityFilter.contains(entity.getClass()))
                continue;

            if(entity.isChild() || !entity.canBreed() || entity.getGrowingAge() != 0)
                continue;

            if(getEntitiesWithinRange(entity.getClass()).size() > mobCap)
                continue;

            if(!breeding.containsKey(entity.getClass()))
                breeding.put(entity.getClass(), new EntityCouple());

            if(!breeding.get(entity.getClass()).canWork())
                continue;

            if(breeding.get(entity.getClass()).increase(entity))
                return;
        }
    }

    private List<AnimalEntity> getAnimalsWithinRange(){
        return getEntitiesWithinRange(AnimalEntity.class);
    }

    private <T extends Entity> List<T> getEntitiesWithinRange(Class<? extends T> clazz){
        AxisAlignedBB area = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius).offset(dispenser.getPos());
        return dispenser.getWorld().getEntitiesWithinAABB(clazz, area);
    }

    public static List<Item> getInteractionItems(){
        return interactionItems;
    }

    public void playerInteract(PlayerInteractEvent event){

        if(event.getItemStack().getItem().equals(Items.BLAZE_ROD)){
            if (event.getPlayer().isSneaking())
                changeRadius(event.getPlayer());
            else
                toggleMode(event.getPlayer());

            event.setCanceled(true);
        }
        else if(event.getItemStack().getItem().equals(Items.NETHER_STAR)){
            if (event.getPlayer().isSneaking()) {
                entityFilter.clear();
                event.getPlayer().sendStatusMessage(new StringTextComponent("filter disabled"), true);
            }
            else
                setupFilter(event.getPlayer());

            event.setCanceled(true);
        }
    }

    private void setupFilter(PlayerEntity player){
        HashMap<Integer, Class> entityClassList = new HashMap<>();

        player.sendStatusMessage(new StringTextComponent("\n§e~~~ dispenser filter setup ~~~"), false);

        if(entityFilter.size() > 0) {
            String existingFilter = "";

            for(Class clazz : entityFilter)
                existingFilter+= ", " + clazz.getSimpleName().replace("Entity", "");

            player.sendStatusMessage(new StringTextComponent("§dalready filtering: §f" + existingFilter.substring(2)), false);
        }
        else
            player.sendStatusMessage(new StringTextComponent("§dalready filtering: §fnone"), false);

        int i=1;
        for(AnimalEntity entity : getAnimalsWithinRange())
            if(!entityFilter.contains(entity.getClass()) && !entityClassList.containsValue(entity.getClass()))
                entityClassList.put(i++, entity.getClass());

        if(entityClassList.size() > 0) {
            player.sendStatusMessage(new StringTextComponent("§aenter number in chat to add Animal to filter or type cancel to abort filter setup"), false);
            for(Map.Entry<Integer, Class> entry : entityClassList.entrySet()) {
                player.sendStatusMessage(new StringTextComponent("[" + entry.getKey() + "] " + entry.getValue().getSimpleName().replace("Entity", "")), false);
            }
            new UserInputEventListener(player.getUniqueID(), entityClassList);
        }
        else
            player.sendStatusMessage(new StringTextComponent("§2no animal found which could be added to filter"), false);
    }

    class UserInputEventListener{
        UUID playerUUID;
        HashMap<Integer, Class> entityClassList = new HashMap<>();

        UserInputEventListener(UUID uuid, HashMap<Integer, Class> list){
            if(chatListeners.containsKey(uuid)){
                //unregister old listeners for the same player
                MinecraftForge.EVENT_BUS.unregister(chatListeners.get(uuid));
                chatListeners.remove(uuid);
            }

            playerUUID = uuid;
            entityClassList.putAll(list);
            MinecraftForge.EVENT_BUS.register(this);
            chatListeners.put(playerUUID, this);
        }

        @SubscribeEvent
        public void onServerChatEvent(ServerChatEvent event) {
            if(!event.getPlayer().getUniqueID().equals(playerUUID))
                return;

            if(event.getMessage().toLowerCase().contains("cancel")){
                event.getPlayer().sendStatusMessage(new StringTextComponent("§6filter setup aborted"), false);
            }
            else {
                try {
                    int id = Integer.valueOf(event.getMessage());
                    if(!entityClassList.containsKey(id)) throw new Exception(){};
                    Class clazz = entityClassList.get(id);
                    entityFilter.add(clazz);
                    event.getPlayer().sendStatusMessage(new StringTextComponent("§aadded §l" + clazz.getSimpleName().replace("Entity", "") + "§r§a to filter"), false);
                }
                catch(Exception ex) {
                    event.getPlayer().sendStatusMessage(new StringTextComponent("§cinvalid input, filter setup aborted"), false);
                }
            }

            event.setCanceled(true);
            MinecraftForge.EVENT_BUS.unregister(this);
        }

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

                if(BetterDispenser.verbose)
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

        int i=0;
        for(Class clazz : entityFilter)
            nbt.putString("filter"+i++, clazz.getName());

        return nbt;
    }

    public void readFromNBT(CompoundNBT nbt){
        if(nbt.contains("active"))
            active = nbt.getBoolean("active");
        if(nbt.contains("radius"))
            radius = nbt.getInt("radius");

        int i=0;
        entityFilter.clear();
        while(nbt.contains("filter"+i)){
            String className = nbt.getString("filter"+i);
            try{
                Class clazz = Class.forName(className);
                entityFilter.add(clazz);
            }
            catch (Exception ex){
                System.out.println("skipping invalid class name for filter '"+className+"'");
            }

            i++;
        }


        updateEventHandler();
    }

}
