package ben_mkiv.betterdispenser.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface IDispenserCapability {
    void onTick(TickEvent.WorldTickEvent event);
    void playerInteract(PlayerInteractEvent event);

    CompoundNBT writeToNBT();
    void readFromNBT(CompoundNBT nbt);
}
