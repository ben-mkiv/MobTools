package ben_mkiv.mobtools.client.gui;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.inventory.container.MobSpawnerContainer;
import ben_mkiv.mobtools.items.MobCartridge;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;

import java.awt.*;
import java.util.ArrayList;

public class MobSpawnerContainerScreen extends CustomContainerScreen<MobSpawnerContainer> {
    public MobSpawnerContainerScreen(MobSpawnerContainer containerBasic, PlayerInventory playerInventory, ITextComponent title){
        super(containerBasic, playerInventory, title, MobSpawnerContainer.width, MobSpawnerContainer.height);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        ItemStack cartridge = container.spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null).getStackInSlot(0);


        if(MobTools.useEnergy){
            IEnergyStorage energyStorage = container.spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
            String energyInfo = "energy: " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored() + " FE";
            font.drawString(matrixStack, energyInfo, getGuiLeft() + container.width - 5 - font.getStringWidth(energyInfo), getGuiTop() + 5, Color.darkGray.getRGB());
        }


        //font.drawString(matrixStack, "mobSpawner", getGuiLeft() + 5, getGuiTop() + 5, Color.darkGray.getRGB());

        String cartridgeInfo = "no cartridge";
        if(cartridge.getItem() instanceof MobCartridge){
            ArrayList<CompoundNBT> mobs = MobCartridge.getStoredEntities(cartridge);
            cartridgeInfo = mobs.size() + " mobs stored";
        }

        font.drawString(matrixStack, cartridgeInfo, getGuiLeft() + container.width/2 - font.getStringWidth(cartridgeInfo) / 2, getGuiTop() + 20, Color.darkGray.getRGB());
    }
}
