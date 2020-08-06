package ben_mkiv.mobtools.inventory;

import ben_mkiv.mobtools.MobTools;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

public class CustomContainerScreen<T extends CustomContainer> extends ContainerScreen<T> {
    public CustomContainerScreen(T containerBasic, PlayerInventory playerInventory, ITextComponent title, int width, int height) {
        super(containerBasic, playerInventory, title);

        // Set the width and height of the gui.  Should match the size of the texture!
        xSize = width;
        ySize = height;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        //this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     * Taken directly from ChestScreen
     */
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        //final float LABEL_XPOS = 5;
        //final float FONT_Y_SPACING = 12;
        //font.drawString(matrixStack, this.title.getUnformattedComponentText(), LABEL_XPOS, 100, Color.darkGray.getRGB());
    }

    /**
     * Draws the background layer of this container (behind the items).
     * Taken directly from ChestScreen / BeaconScreen
     */
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);

        // width and height are the size provided to the window when initialised after creation.
        // xSize, ySize are the expected size of the texture-? usually seems to be left as a default.
        // The code below is typical for vanilla containers, so I've just copied that- it appears to centre the texture within
        //  the available window
        int edgeSpacingX = (this.width - this.xSize) / 2;
        int edgeSpacingY = (this.height - this.ySize) / 2;
        this.blit(matrixStack, edgeSpacingX, edgeSpacingY, 0, 0, this.xSize, this.ySize);
    }

    // This is the resource location for the background image for the GUI
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(MobTools.MOD_ID, "textures/gui/mobcollector_inventory.png");
}
