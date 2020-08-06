package ben_mkiv.mobtools.tileentity;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.inventory.MobCollectorInventory;
import ben_mkiv.mobtools.items.MobCartridge;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;

public class SpawnerTileEntity extends TileEntity implements ITickableTileEntity {
    public static TileEntityType<SpawnerTileEntity> tileEntityType;
    public static int entityCountLimit = 30;

    public MobCollectorInventory inventory = new MobCollectorInventory(1, this);

    ArrayList<String> mobTypes = new ArrayList<>();

    double radius = 4;

    public SpawnerTileEntity(){
        super(tileEntityType);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put("inventory", inventory.serializeNBT());

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
        }
        super.read(state, compound);
    }

    @Override
    public void tick() {
        if(getWorld().getGameTime() % 20 != 0)
            return;

        AxisAlignedBB area = new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(radius);

        int entityCount = getWorld().getEntitiesWithinAABB(MobEntity.class, area).size();


        for(String type : mobTypes){
            entityCount++;

            if(entityCount >= entityCountLimit)
                break;

            EntityType mobType = EntityType.byKey(type).orElse(null);

            Entity mob = mobType.create(getWorld());

            Vector3d spawnPosition = new Vector3d(getPos().getX(), getPos().getY(), getPos().getZ());

            spawnPosition = spawnPosition.add(getWorld().rand.nextFloat() * radius, 1, getWorld().rand.nextFloat() * radius);

            mob.setPosition(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());

            getWorld().addEntity(mob);
        }

    }

    public void reloadInventory(){
        mobTypes.clear();
        for(CompoundNBT nbt : MobCartridge.getStoredEntities(inventory.getStackInSlot(0))){
            mobTypes.add(nbt.getString("id"));
        }
    }
}
