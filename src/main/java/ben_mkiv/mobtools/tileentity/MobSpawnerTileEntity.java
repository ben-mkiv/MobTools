package ben_mkiv.mobtools.tileentity;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.blocks.MobSpawnerBlock;
import ben_mkiv.mobtools.energy.CustomEnergyStorage;
import ben_mkiv.mobtools.interfaces.IContentListener;
import ben_mkiv.mobtools.inventory.MobSpawnerInventory;
import ben_mkiv.mobtools.inventory.container.MobSpawnerContainer;
import ben_mkiv.mobtools.items.MobCartridge;
import ben_mkiv.mobtools.items.MobSpawnerItem;
import ben_mkiv.mobtools.utils.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class MobSpawnerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IContentListener, ITilePacketHandler {
    public static TileEntityType<MobSpawnerTileEntity> tileEntityType;

    private MobSpawnerInventory inventory = new MobSpawnerInventory(this);
    private final LazyOptional<IItemHandler> inventorySupplier = LazyOptional.of(() -> inventory);

    private CustomEnergyStorage energyStorage = new CustomEnergyStorage(1000000, this);
    private final LazyOptional<IEnergyStorage> energySupplier = LazyOptional.of(() -> energyStorage);

    private ArrayList<String> mobTypes = new ArrayList<>();

    public int radius;
    public int tickDelay;
    public boolean isRedstonePowered = false;

    public boolean upgradeSpeed = false, upgradeRange = false;

    public boolean readyToWork = false;

    private long lastSpawnTime = 0;

    public MobSpawnerTileEntity(){
        super(tileEntityType);
        setRadius(7);
        setDelay(200);
    }

    @Override
    public void handleNetworkUpdate(CompoundNBT data) {
        if(data.contains("setRadius"))
            setRadius(data.getInt("setRadius"));
        if(data.contains("setDelay"))
            setDelay(data.getInt("setDelay"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put("inventory", inventory.serializeNBT());
        nbt.put("energyStorage", energyStorage.serializeNBT());

        nbt.putInt("tickDelay", tickDelay);
        nbt.putInt("radius", radius);

        compound.put(MobTools.MOD_ID, nbt);

        return super.write(compound);
    }

    public int getMaxRadius(){
        if(MobTools.badPlacementPenalty)
            return (int) Math.round(Math.min(MobTools.spawnerMaxRadius * (upgradeRange ? 1 : 0.5), MobSpawnerItem.maxChunkRadius(getPos())));
        else
            return (int) Math.round(MobTools.spawnerMaxRadius * (upgradeRange ? 1 : 0.5));
    }

    public int getMinTickDelay(){
        return upgradeSpeed ? MobTools.spawnerMinTickDelay : MobTools.spawnerMinTickDelay + ((MobTools.spawnerMaxTickDelay - MobTools.spawnerMinTickDelay) / 2);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        if(compound.contains(MobTools.MOD_ID)) {
            CompoundNBT nbt = compound.getCompound(MobTools.MOD_ID);

            if(nbt.contains("inventory"))
                inventory.deserializeNBT(nbt.getCompound("inventory"));

            // fix old inventories
            if(inventory.getSlots() == 1){
                ItemStack cartridge = inventory.getStackInSlot(0);
                inventory = new MobSpawnerInventory(this);
                inventory.setStackInSlot(0, cartridge);
            }

            if(nbt.contains("energyStorage"))
                energyStorage.deserializeNBT(nbt.getCompound("energyStorage"));

            if(nbt.contains("tickDelay"))
                tickDelay = nbt.getInt("tickDelay");

            if(nbt.contains("radius"))
                radius = nbt.getInt("radius");

            reloadInventory(1); // check for range upgrade
            reloadInventory(2); // check for speed upgrade
        }
        super.read(state, compound);
    }

    private void setRadius(int newRadius){
        radius = Math.min(getMaxRadius(), newRadius);
        markDirty();
    }

    private void setDelay(int newDelay){
        tickDelay = Math.max(getMinTickDelay(), Math.min(MobTools.spawnerMaxTickDelay, newDelay));
        if(getWorld() != null)
            lastSpawnTime = getWorld().getGameTime();
        markDirty();
    }

    @Override
    public void tick() {
        if(getWorld().isRemote())
            return;

        if(!readyToWork){
            MobSpawnerBlock.updateRedstoneState(getWorld(), getPos());
            if(MobTools.badPlacementPenalty){
                setRadius(Math.min(radius, MobSpawnerItem.maxChunkRadius(getPos())));;
            }
            reloadMobTypes();
            readyToWork = true;
        }

        if(isRedstonePowered)
            return;

        if(getWorld().getGameTime() - lastSpawnTime < tickDelay)
            return;

        lastSpawnTime = getWorld().getGameTime();

        if(mobTypes.isEmpty())
            return;

        AxisAlignedBB area = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(MobTools.spawnerMaxRadius).offset(getPos());

        int entityCount = getWorld().getEntitiesWithinAABB(MobEntity.class, area).size();

        for(String type : mobTypes){
            entityCount++;

            if(entityCount > MobTools.entityCountLimit)
                break;

            Entity mob = createEntityByType(getWorld(), type);

            if(mob == null)
                continue;

            if(!(mob instanceof MobEntity))
                continue;

            Vector3d spawnPosition = Vector3d.copy(getPos()).subtract(radius, 0, radius).add(getWorld().rand.nextFloat() * 2 * radius, 1, getWorld().rand.nextFloat() * 2 * radius);

            mob.setPosition(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());

            if(!((MobEntity) mob).canSpawn(getWorld(), SpawnReason.SPAWN_EGG))
                continue;

            if(!getWorld().hasNoCollisions(mob))
                continue;

            if(MobTools.useEnergy) {
                int energyCost = (int) Math.ceil(((MobEntity) mob).getHealth() * MobTools.energyBaseCost);

                if(energyStorage.extractEnergy(energyCost, true) != energyCost)
                    continue;

                energyStorage.extractEnergy(energyCost, false);
            }



            getWorld().addEntity(mob);
        }
    }

    public void setRedstonePowered(boolean isPowered){
        isRedstonePowered = isPowered;
    }

    private void reloadMobTypes(){
        mobTypes.clear();
        for (CompoundNBT nbt : MobCartridge.getStoredEntities(inventory.getStackInSlot(0))) {
            String type = nbt.getString("id");
            Entity temporaryEntity = createEntityByType(getWorld(), type);
            if (temporaryEntity == null)
                continue;

            if (!MobTools.allowBossSpawn && !temporaryEntity.isNonBoss())
                continue;

            mobTypes.add(type);
        }
    }

    public void reloadInventory(int slot){
        switch(slot) {
            case 0:
                reloadMobTypes();
                break;
            case 1:
                upgradeRange = !inventory.getStackInSlot(1).isEmpty();
                setRadius(radius); // update radius with current value so that it gets capped when necessary
                break;
            case 2:
                upgradeSpeed = !inventory.getStackInSlot(2).isEmpty();
                setDelay(tickDelay); // update delay with current value so that it gets capped when necessary
                break;

        }
    }

    private static Entity createEntityByType(World world, String type){
        EntityType mobType = EntityType.byKey(type).orElse(null);
        return mobType != null ? mobType.create(world) : null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return inventorySupplier.cast();

        if (capability == CapabilityEnergy.ENERGY)
            return energySupplier.cast();

        return super.getCapability(capability, side);
    }

    @Override
    public void remove() {
        if(getWorld().isRemote())
            return;

        ItemUtils.dropInventory(getWorld(), inventory, getPos(), false, 10);

        super.remove();
    }

    public void onContentChanged(boolean forceBlockUpdate){
        markDirty();
        if(forceBlockUpdate) sendBlockUpdate();
    }

    public void sendBlockUpdate(){
        BlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public @Nonnull
    ITextComponent getDisplayName(){
        return new StringTextComponent("mobSpawner");
    }

    @Override
    @Nullable
    public Container createMenu(int p_createMenu_1_, PlayerInventory playerInventory, PlayerEntity playerEntity){
        return new MobSpawnerContainer(playerInventory, this);
    }

}
