package ben_mkiv.mobtools.items;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.inventory.container.MobCollectorContainer;
import ben_mkiv.mobtools.inventory.MobCollectorItemInventory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MobCollector extends Item implements INamedContainerProvider {

    public static MobCollector DEFAULT;

    static final int defaultRange = 16;
    public static int GUI_ID = 1;

    public MobCollector(){
        super(new Properties().maxStackSize(1).group(MobTools.CREATIVE_TAB));
        setRegistryName(MobTools.MOD_ID, "mobcollector");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(new StringTextComponent("§5sneak + right-click §7to change mobcartridge"));
        tooltip.add(new StringTextComponent("§5right-click §7to capture mob"));
        tooltip.add(new StringTextComponent("§5left-click §7to release last captured mob"));
    }


    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        //if(world.isRemote())
        //    return ActionResult.resultSuccess(player.getHeldItem(hand));

        if(player.isSneaking()){
            if(!world.isRemote()) {
                openInventory(player.getHeldItem(hand), player);
            }
            return ActionResult.resultFail(player.inventory.getCurrentItem());
        }
        else {
            MobEntity result = rayTrace(player, player.getHeldItem(hand));

            if(result != null){
                Vector3d pos = result.getEntity().getPositionVec();

                for(int i=0; i < 10; i++) {
                    AxisAlignedBB aabb = result.getEntity().getBoundingBox();

                    world.addParticle(ParticleTypes.FLAME, pos.x - 0.5 + world.rand.nextFloat(), pos.y - 0.5 + world.rand.nextFloat(), pos.z - 0.5 + world.rand.nextFloat(), 0, 0.1, 0);
                }

                if(!player.getEntityWorld().isRemote()) {
                    if(!MobTools.allowBossCapture && !result.isNonBoss())
                        player.sendStatusMessage(new StringTextComponent(result.getDisplayName().getUnformattedComponentText() + " is a boss mob and can't be stored"), true);
                    else if(result instanceof TameableEntity && ((TameableEntity) result).getOwnerId() != null && !((TameableEntity) result).isOwner(player))
                        player.sendStatusMessage(new StringTextComponent(result.getDisplayName().getUnformattedComponentText() + " is owned by another player and can't be stored"), true);
                    else if(storeMob(player, player.getHeldItem(hand), result))
                        player.sendStatusMessage(new StringTextComponent(result.getDisplayName().getUnformattedComponentText() + " stored"), true);
                    else
                        player.sendStatusMessage(new StringTextComponent(result.getDisplayName().getUnformattedComponentText() + " not stored"), true);
                }
            }

            return ActionResult.resultFail(player.getHeldItem(hand));
        }
    }

    public static ItemStack getCartridge(ItemStack mobCollectorStack){
        if(!(mobCollectorStack.getItem() instanceof MobCollector))
            return ItemStack.EMPTY;

        MobCollectorItemInventory inventory = new MobCollectorItemInventory(mobCollectorStack);

        return inventory.getStackInSlot(0);
    }

    public static void updateCartridge(ItemStack mobCollectorStack, ItemStack cartridge){
        if(!(mobCollectorStack.getItem() instanceof MobCollector))
            return;

        MobCollectorItemInventory inventory = new MobCollectorItemInventory(mobCollectorStack);
        inventory.setStackInSlot(0, cartridge);
    }


    public static boolean storeMob(PlayerEntity player, ItemStack stack, Entity entity){
        ItemStack cartridge = getCartridge(stack);

        if(cartridge.isEmpty()) {
            player.sendStatusMessage(new StringTextComponent("no mob cartridge, " + entity.getDisplayName() + " not stored"), true);
            return false;
        }

        if(!MobCartridge.storeMob(cartridge, entity))
            return false;

        updateCartridge(stack, cartridge);

        entity.remove();

        return true;
    }

    public static MobEntity rayTrace(Entity source, ItemStack itemStack){
        RayTraceResult result = raytrace(source, MobCollector::isEntityValid, getRange(itemStack));

        return result.getType().equals(RayTraceResult.Type.ENTITY) ? (MobEntity) ((EntityRayTraceResult) result).getEntity() : null;
    }

    public static RayTraceResult raytraceBlocks(Entity sourceEntity, double range){
        World world = sourceEntity.world;
        Vector3d currentPosition = sourceEntity.getEyePosition(1);

        Vector3d vector3d2 = currentPosition.add(sourceEntity.getLookVec().scale(range));

        return world.rayTraceBlocks(new RayTraceContext(currentPosition, vector3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, sourceEntity));
    }

    public static RayTraceResult raytraceEntity(Entity sourceEntity, Predicate<Entity> entityPredicate, double range){
        World world = sourceEntity.world;
        Vector3d currentPosition = sourceEntity.getEyePosition(1);

        Vector3d vector3d2 = currentPosition.add(sourceEntity.getLookVec().scale(range));

        return ProjectileHelper.rayTraceEntities(world, sourceEntity, currentPosition, vector3d2, sourceEntity.getBoundingBox().grow(range), entityPredicate);
    }

    public static RayTraceResult raytrace(Entity sourceEntity, Predicate<Entity> entityPredicate, int range) {
        double distance = range;

        RayTraceResult raytraceresultBlock = raytraceBlocks(sourceEntity, range);
        // limit area to the first hit block
        if (raytraceresultBlock.getType() != RayTraceResult.Type.MISS) {
            distance = sourceEntity.getEyePosition(1).distanceTo(raytraceresultBlock.getHitVec());
        }

        // trace for entities
        RayTraceResult raytraceresultEntity = raytraceEntity(sourceEntity, entityPredicate, distance);

        return raytraceresultEntity != null ? raytraceresultEntity : raytraceresultBlock;
    }


    public static boolean isEntityValid(Entity entity){
        return entity instanceof MobEntity;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity playerEntity) {

        if(!playerEntity.getEntityWorld().isRemote()){
            ItemStack cartridge = getCartridge(stack);

            if(cartridge.isEmpty())
                return false;

            RayTraceResult rayTraceResult = raytraceBlocks(playerEntity, getRange(stack));

            if(rayTraceResult.getType().equals(RayTraceResult.Type.MISS))
                return false;

            CompoundNBT entityNBT = MobCartridge.extractMob(cartridge);

            if(entityNBT != null){
                EntityType type = EntityType.byKey(entityNBT.getString("id")).orElse(null);
                if (type != null) {
                    Entity mob = type.create(playerEntity.getEntityWorld());
                    mob.read(entityNBT);

                    mob.setPosition(rayTraceResult.getHitVec().getX(), rayTraceResult.getHitVec().getY(), rayTraceResult.getHitVec().getZ());
                    playerEntity.getEntityWorld().addEntity(mob);

                    if(playerEntity instanceof PlayerEntity)
                        ((PlayerEntity) playerEntity).sendStatusMessage(new StringTextComponent("releasing entity"), true);
                }
            }
        }

        return false;
    }


    public static void openInventory(ItemStack stack, PlayerEntity player){
        if(player.getEntityWorld().isRemote())
            return;

        Consumer<PacketBuffer> extraData = (PacketBuffer packetBuffer) -> {
            packetBuffer.writeItemStack(stack);
        };

        NetworkHooks.openGui((ServerPlayerEntity) player, (MobCollector) stack.getItem(), extraData);
    }


    public static int getRange(ItemStack stack){
        return stack.hasTag() && stack.getTag().contains("range") ? stack.getTag().getInt("range") : defaultRange;
    }


    @Override
    public @Nonnull
    ITextComponent getDisplayName(){
        return new StringTextComponent("mobCollector");
    }

    @Override
    @Nullable
    public Container createMenu(int p_createMenu_1_, PlayerInventory playerInventory, PlayerEntity playerEntity){
        return new MobCollectorContainer(playerEntity, playerInventory, playerEntity.getHeldItemMainhand());
    }

}
