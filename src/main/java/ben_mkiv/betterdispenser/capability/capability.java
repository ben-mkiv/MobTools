package ben_mkiv.betterdispenser.capability;

import ben_mkiv.betterdispenser.BetterDispenser;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class capability implements ICapabilitySerializable<INBT> {
    public static final String NAME = "betterdispenser_capability";
    public static ResourceLocation DISPENSER_CAPABILITY = new ResourceLocation(BetterDispenser.MOD_ID, NAME);

    private LazyOptional<DispenserCapability> lazyInstance = LazyOptional.empty();

    DispenserCapability instance = null;

    @CapabilityInject(IDispenserCapability.class)
    public static Capability<IDispenserCapability> CAPABILITY = null;

    public capability(DispenserTileEntity dispenser){
        instance = new DispenserCapability(dispenser);
        lazyInstance = LazyOptional.of(() -> instance).cast();
    }

    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side){
        if(cap == capability.CAPABILITY)
            return LazyOptional.of(() -> instance).cast();
        else
            return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        // @formatter:off
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, lazyInstance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null);
        // @formatter:on
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        // @formatter:off
        CAPABILITY.getStorage().readNBT(CAPABILITY, lazyInstance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")), null, nbt);
        // @formatter:on
    }

    public static class Storage implements Capability.IStorage<IDispenserCapability> {
        @Override
        public INBT writeNBT(Capability<IDispenserCapability> capability, IDispenserCapability instance, Direction side) {
            return instance.writeToNBT();
        }

        @Override
        public void readNBT(Capability<IDispenserCapability> capability, IDispenserCapability instance, Direction side, INBT nbt) {
            if(nbt instanceof CompoundNBT)
                instance.readFromNBT((CompoundNBT) nbt);
        }
    }

    public static class Factory implements Callable<IDispenserCapability> {
        @Override
        public IDispenserCapability call() throws Exception {
            return new DispenserCapability(null);
        }
    }
}
