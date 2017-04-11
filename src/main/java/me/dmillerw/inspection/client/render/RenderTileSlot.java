package me.dmillerw.inspection.client.render;

import me.dmillerw.inspection.block.BlockSlot;
import me.dmillerw.inspection.block.tile.TileSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

/**
 * @author dmillerw
 */
public class RenderTileSlot extends TileEntitySpecialRenderer<TileSlot> {

    @Override
    public void renderTileEntityAt(TileSlot te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te.renderItem != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            final EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockSlot.FACING);
            float angle = 0;

            if (facing == EnumFacing.NORTH)
                angle = 180;
            else if (facing == EnumFacing.EAST)
                angle = 90;
            else if (facing == EnumFacing.WEST)
                angle = 270;

            GlStateManager.pushMatrix();

            GlStateManager.translate(.5f, .5f, .5f);
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.translate(-.5f, -.5f, -.5f);

            GlStateManager.translate(0, 1, 1);
            GlStateManager.scale(1 / 16f, -1 / 16f, 0.00001);

            GlStateManager.translate(4, 4, 0.);
            GlStateManager.scale(0.5, 0.5, 1);

            GlStateManager.pushMatrix();

            GlStateManager.scale(2.6f, 2.6f, 1);
            GlStateManager.rotate(171.6f, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(84.9f, 1.0F, 0.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();

            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-1, -1);

            GlStateManager.pushAttrib();
            GlStateManager.enableRescaleNormal();
            GlStateManager.popAttrib();

            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(te.renderItem, 0, 0);

            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();

            GlStateManager.disablePolygonOffset();

            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
        }
    }
}
