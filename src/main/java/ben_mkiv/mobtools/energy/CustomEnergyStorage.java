package ben_mkiv.mobtools.energy;

import ben_mkiv.mobtools.interfaces.IContentListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {
    IContentListener listener;

    public CustomEnergyStorage(int capacity, IContentListener contentListener){
        super(capacity);
        listener = contentListener;
    }

    public CompoundNBT serializeNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("capacity", capacity);
        nbt.putInt("energy", energy);
        nbt.putInt("maxReceive", maxReceive);
        nbt.putInt("maxExtract", maxExtract);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT nbt){
        if(nbt.contains("capacity"))
            capacity = nbt.getInt("capacity");
        if(nbt.contains("energy"))
            energy = nbt.getInt("energy");
        if(nbt.contains("maxReceive"))
            maxReceive = nbt.getInt("maxReceive");
        if(nbt.contains("maxExtract"))
            maxExtract = nbt.getInt("maxExtract");
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);

        if(!simulate && received != 0 && listener != null)
            listener.onContentChanged(false);

        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);

        if(!simulate && extracted != 0 && listener != null)
            listener.onContentChanged(false);

        return extracted;
    }

    public void setEnergyStored(int energyStored){
        energy = energyStored;
    }
}
