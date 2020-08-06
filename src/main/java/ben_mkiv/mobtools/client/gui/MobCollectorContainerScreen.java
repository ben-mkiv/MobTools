package ben_mkiv.mobtools.client.gui;

import ben_mkiv.mobtools.inventory.container.MobCollectorContainer;
import ben_mkiv.mobtools.items.MobCartridge;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;

public class MobCollectorContainerScreen extends CustomContainerScreen<MobCollectorContainer> {
    public MobCollectorContainerScreen(MobCollectorContainer containerBasic, PlayerInventory playerInventory, ITextComponent title){
        super(containerBasic, playerInventory, title, MobCollectorContainer.width, MobCollectorContainer.height);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        String text = "no cartridge";
        ItemStack cartridge = container.inventory.getStackInSlot(0);

        if(cartridge.getItem() instanceof MobCartridge){
            ArrayList<CompoundNBT> mobs = MobCartridge.getStoredEntities(cartridge);
            text = mobs.size() + " mobs stored";
        }


        font.drawString(matrixStack, "mobCollector", getGuiLeft() + 5, getGuiTop() + 5, Color.darkGray.getRGB());
        font.drawString(matrixStack, text, width/2 - font.getStringWidth(text) / 2, getGuiTop() + 20, Color.darkGray.getRGB());
    }
}
