package ben_mkiv.betterdispenser.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent;

public interface IDispenserCapability {
    void onTick(TickEvent.WorldTickEvent event);
    void playerInteract(PlayerEntity player);

    CompoundNBT writeToNBT();
    void readFromNBT(CompoundNBT nbt);
}
