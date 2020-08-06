package ben_mkiv.mobtools.tileentity;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.energy.CustomEnergyStorage;
import ben_mkiv.mobtools.interfaces.IContentListener;
import ben_mkiv.mobtools.inventory.MobCollectorInventory;
import ben_mkiv.mobtools.inventory.container.MobSpawnerContainer;
import ben_mkiv.mobtools.items.MobCartridge;
import ben_mkiv.mobtools.utils.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
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

public class MobSpawnerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IContentListener {
    public static TileEntityType<MobSpawnerTileEntity> tileEntityType;

    private MobCollectorInventory inventory = new MobCollectorInventory(1, this);
    private final LazyOptional<IItemHandler> inventorySupplier = LazyOptional.of(() -> inventory);

    private CustomEnergyStorage energyStorage = new CustomEnergyStorage(1000000, this);
    private final LazyOptional<IEnergyStorage> energySupplier = LazyOptional.of(() -> energyStorage);

    private ArrayList<String> mobTypes = new ArrayList<>();

    private int radius = 4;
    private int tickDelay = 200;

    public MobSpawnerTileEntity(){
        super(tileEntityType);
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

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        if(compound.contains(MobTools.MOD_ID)) {
            CompoundNBT nbt = compound.getCompound(MobTools.MOD_ID);

            if(nbt.contains("inventory")) {
                inventory.deserializeNBT(nbt.getCompound("inventory"));
            }

            if(nbt.contains("energyStorage")) {
                energyStorage.deserializeNBT(nbt.getCompound("energyStorage"));
            }

            if(nbt.contains("tickDelay"))
                tickDelay = nbt.getInt("tickDelay");

            if(nbt.contains("radius"))
                radius = nbt.getInt("radius");

        }
        super.read(state, compound);
    }

    @Override
    public void tick() {
        if(getWorld().getGameTime() % tickDelay != 0)
            return;

        AxisAlignedBB area = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius);

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

            if(MobTools.useEnergy) {
                int energyCost = (int) Math.ceil(((MobEntity) mob).getHealth() * MobTools.energyBaseCost);

                if(energyStorage.extractEnergy(energyCost, true) != energyCost)
                    continue;

                energyStorage.extractEnergy(energyCost, false);
            }

            Vector3d spawnPosition = Vector3d.copy(getPos()).add(getWorld().rand.nextFloat() * radius, 1, getWorld().rand.nextFloat() * radius);

            mob.setPosition(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());

            getWorld().addEntity(mob);
        }

    }

    public void reloadInventory(){
        mobTypes.clear();
        for(CompoundNBT nbt : MobCartridge.getStoredEntities(inventory.getStackInSlot(0))){
            String type = nbt.getString("id");
            Entity temporaryEntity = createEntityByType(getWorld(), type);
            if(temporaryEntity == null)
                continue;

            if(!MobTools.allowBossSpawn && !temporaryEntity.isNonBoss())
                continue;

            mobTypes.add(type);
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

    /*
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 42, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(getWorld().getBlockState(pkt.getPos()), pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state, tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(super.getUpdateTag());
    }
    */

    @Override
    public @Nonnull
    ITextComponent getDisplayName(){
        return new StringTextComponent("mobSpawner");
    }

    @Override
    @Nullable
    public Container createMenu(int p_createMenu_1_, PlayerInventory playerInventory, PlayerEntity playerEntity){
        return new MobSpawnerContainer(playerEntity, playerInventory, this);
    }

}
