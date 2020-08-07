package ben_mkiv.mobtools.client.gui;

import net.minecraft.util.IntReferenceHolder;

// this wrapper forces a sync by returning true for the first isDirty() check, so even values of 0 get synced
public abstract class IntReferenceHolderSmart extends IntReferenceHolder {
    private boolean initialSyncDone = false;

    public boolean isDirty() {
        if(!initialSyncDone){
            initialSyncDone = true;
            return true;
        }
        return super.isDirty();
    }
}
