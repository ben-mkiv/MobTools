package ben_mkiv.mobtools.inventory.slots;

import net.minecraft.util.text.ITextProperties;

import java.util.List;

public interface ISlotTooltip {
    List<? extends ITextProperties> getTooltip();
}
