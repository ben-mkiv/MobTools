package ben_mkiv.mobtools.blocks;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.inventory.MobSpawnerContainer;
import ben_mkiv.mobtools.tileentity.SpawnerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SpawnerBlock extends Block{
    public static SpawnerBlock DEFAULT;
    public static int GUI_ID = 2;

    public SpawnerBlock(){
        super(Properties.create(Material.IRON));
        setRegistryName(MobTools.MOD_ID, "spawner");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SpawnerTileEntity();
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if(player.getEntityWorld().isRemote())
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);

        Consumer<PacketBuffer> extraData = (PacketBuffer packetBuffer) -> {
            packetBuffer.writeBlockPos(pos);
        };

        SpawnerTileEntity tile = (SpawnerTileEntity) worldIn.getTileEntity(pos);

        if(tile != null) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new MobSpawnerContainer(player, player.inventory, tile), extraData);
            return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

}
