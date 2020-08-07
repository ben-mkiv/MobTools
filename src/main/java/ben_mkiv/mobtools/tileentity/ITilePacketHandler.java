package ben_mkiv.mobtools.tileentity;

import net.minecraft.nbt.CompoundNBT;

public interface ITilePacketHandler {
    void handleNetworkUpdate(CompoundNBT data);
}
