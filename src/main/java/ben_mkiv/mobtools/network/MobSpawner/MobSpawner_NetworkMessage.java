package ben_mkiv.mobtools.network.MobSpawner;

import ben_mkiv.mobtools.tileentity.MobSpawnerTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MobSpawner_NetworkMessage {
    private final CompoundNBT updateTag;
    private final BlockPos pos;

    public MobSpawner_NetworkMessage(MobSpawnerTileEntity tile, CompoundNBT updateTag) {
        this.pos = tile.getPos();
        this.updateTag = updateTag;
    }

    public MobSpawner_NetworkMessage(BlockPos pos, CompoundNBT updateTag) {
        this.pos = pos;
        this.updateTag = updateTag;
    }

    public static void handle(MobSpawner_NetworkMessage message, Supplier<NetworkEvent.Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            TileEntity tile = player.getEntityWorld().getTileEntity(message.pos);
            if (tile instanceof MobSpawnerTileEntity) {
                ((MobSpawnerTileEntity) tile).handleNetworkUpdate(message.updateTag);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(MobSpawner_NetworkMessage pkt, PacketBuffer buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeCompoundTag(pkt.updateTag);
    }


    public static MobSpawner_NetworkMessage decode(PacketBuffer buf) {
        return new MobSpawner_NetworkMessage(buf.readBlockPos(), buf.readCompoundTag());
    }
}
