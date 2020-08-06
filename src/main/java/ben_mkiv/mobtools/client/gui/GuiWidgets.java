package ben_mkiv.mobtools.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class GuiWidgets {
    public static void ProgressBar2D_horizontal(int x, int y, int width, int height, int percent, Color bgColor, Color fgColor){
        renderRectangle(new Rectangle2d(x, y, width, height), bgColor);
        renderRectangle(new Rectangle2d(x+1, y+1, (int) Math.round((double) (width-2)/100d * percent), height-2), fgColor);
    }

    public static void ProgressBar2D_vertical(int x, int y, int width, int height, int percent, Color bgColor, Color fgColor){
        renderRectangle(new Rectangle2d(x, y, width, height), bgColor);
        int percHeight = (int) Math.round((double) (height-2)/100d * percent);
        renderRectangle(new Rectangle2d(x+1, y+height-1-percHeight, width-2, percHeight), fgColor);
    }

    public static void renderRectangle(Rectangle2d rectangle, Color color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.color4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);

        int i = rectangle.getX();
        int j = rectangle.getY();
        int k = i + rectangle.getWidth();
        int l = j + rectangle.getHeight();

        bufferbuilder.pos((double)i, (double)l, 0.0D).endVertex();
        bufferbuilder.pos((double)k, (double)l, 0.0D).endVertex();
        bufferbuilder.pos((double)k, (double)j, 0.0D).endVertex();
        bufferbuilder.pos((double)i, (double)j, 0.0D).endVertex();

        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }
}
