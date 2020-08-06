package ben_mkiv.mobtools.inventory.slots;

import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.*;

public class SpecialItemSlot extends SlotItemHandler implements ISlotTooltip {
    private Set<Item> itemWhitelist = new HashSet<>();

    public SpecialItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Set<Item> acceptedItems){
        super(itemHandler, index, xPosition, yPosition);
        itemWhitelist.addAll(acceptedItems);
    }

    @Override
    public List<? extends ITextProperties> getTooltip() {
        ArrayList<ITextProperties> tooltips = new ArrayList<>();

        if(itemWhitelist.size() > 0)
            tooltips.add(new StringTextComponent("accepted items:"));

        for(Item item : itemWhitelist){
            tooltips.add(item.getName());
        }

        return tooltips;
    }
}
