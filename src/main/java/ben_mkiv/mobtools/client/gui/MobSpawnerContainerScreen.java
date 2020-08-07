package ben_mkiv.mobtools.client.gui;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.inventory.container.MobSpawnerContainer;
import ben_mkiv.mobtools.items.MobCartridge;
import ben_mkiv.mobtools.network.MobSpawner.MobSpawner_NetworkMessage;
import ben_mkiv.mobtools.network.NetworkPacketBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.items.CapabilityItemHandler;

import java.awt.*;
import java.util.ArrayList;

public class MobSpawnerContainerScreen extends CustomContainerScreen<MobSpawnerContainer> {
    Slider rangeSlider;

    class IgnorantHandler implements Button.IPressable {
        @Override
        public void onPress(Button element) {}
    };

    class SliderHandler implements Slider.ISlider {
        @Override
        public void onChangeSliderValue(Slider slider) {
            CompoundNBT data = new CompoundNBT();
            data.putInt("setRadius", slider.getValueInt());
            NetworkPacketBase.sendToServer(new MobSpawner_NetworkMessage(container.spawner, data));
            System.out.println("value changed to " + slider.getValueInt());
        }
    };

    SliderHandler rangeSliderHandler = new SliderHandler();

    public MobSpawnerContainerScreen(MobSpawnerContainer containerBasic, PlayerInventory playerInventory, ITextComponent title){
        super(containerBasic, playerInventory, title, MobSpawnerContainer.width, MobSpawnerContainer.height);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        buttons.clear();

        rangeSlider = new Slider(getGuiLeft() + 5, getGuiTop() + 90, 110, 16, new StringTextComponent("radius: "), new StringTextComponent(" blocks"), 0, container.spawner.getMaxRadius(), container.spawner.radius, false, true, new IgnorantHandler(), rangeSliderHandler);

        if(!buttons.contains(rangeSlider))
            addButton(rangeSlider);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);

        ItemStack cartridge = container.spawner.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null).getStackInSlot(0);

        if(MobTools.useEnergy){
            IEnergyStorage energyStorage = container.spawner.getCapability(CapabilityEnergy.ENERGY).orElse(null);
            String energyInfo = "energy: " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored() + " FE";

            int perc = (int) Math.round((double) energyStorage.getEnergyStored()/(double) energyStorage.getMaxEnergyStored() * 100d);
            GuiWidgets.ProgressBar2D_horizontal(getGuiLeft() + 5, getGuiTop() + 5, container.width - 10, 13, perc, Color.GRAY, Color.GREEN);

            font.drawString(matrixStack, energyInfo, getGuiLeft() + container.width - 15 - font.getStringWidth(energyInfo), getGuiTop() + 8, Color.darkGray.getRGB());
        }



        if(cartridge.getItem() instanceof MobCartridge){
            //HashMap<String, Integer> mobs = MobCartridge.getStoredEntitiesCount(cartridge);
            ArrayList<CompoundNBT> mobs = MobCartridge.getStoredEntities(cartridge);

            if(mobs.isEmpty())
                font.drawString(matrixStack, "no mobs stored", getGuiLeft() + 10, getGuiTop() + 25, Color.DARK_GRAY.getRGB());
            else {
                font.drawString(matrixStack, mobs.size() + " mobs stored", getGuiLeft() + 10, getGuiTop() + 25, Color.DARK_GRAY.getRGB());
                /*
                int offsetY = 0;
                for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
                    String id = entry.getKey().contains(":") ? entry.getKey().split(":")[1] : entry.getKey();
                    font.drawString(matrixStack, entry.getValue() + "x " + id, getGuiLeft() + 10, getGuiTop() + 25 + offsetY, Color.DARK_GRAY.getRGB());
                    offsetY += 10;
                }*/

                int workPerc = container.spawner.isRedstonePowered ? 100 : (int) Math.round((double) (container.spawner.getWorld().getGameTime() % container.spawner.tickDelay) / (double) container.spawner.tickDelay * 100d);
                GuiWidgets.ProgressBar2D_vertical(getGuiLeft() + 135, getGuiTop() + 76, 4, 26, workPerc, Color.GRAY, container.spawner.isRedstonePowered ? Color.RED : Color.GREEN);
            }
        }
        else {
            font.drawString(matrixStack, "no cartridge", getGuiLeft() + 10, getGuiTop() + 25, Color.darkGray.getRGB());
        }

    }
}
