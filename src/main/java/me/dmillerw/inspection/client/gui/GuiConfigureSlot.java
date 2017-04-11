package me.dmillerw.inspection.client.gui;

import com.google.common.collect.Lists;
import me.dmillerw.inspection.block.tile.TileSlot;
import me.dmillerw.inspection.client.gui.widget.GuiButtonArrow;
import me.dmillerw.inspection.lib.ModInfo;
import me.dmillerw.inspection.network.PacketHandler;
import me.dmillerw.inspection.network.packet.server.SConfigureSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.List;

/**
 * @author dmillerw
 */
public class GuiConfigureSlot extends GuiScreen {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ModInfo.MOD_ID, "textures/gui/slot_debug.png");

    private static final int BUTTON_SLOT_DOWN = 0;
    private static final int BUTTON_SLOT_UP = 1;

    public static final int GUI_WIDTH = 118;
    public static final int GUI_HEIGHT = 122;

    private TileSlot tileSlot;
    private ItemStack[] blockCache;
    private int slot = 0;

    private int guiLeft;
    private int guiTop;

    public GuiConfigureSlot(TileSlot tileSlot) {
        this.tileSlot = tileSlot;

        this.blockCache = new ItemStack[15];
        for (int i = 0; i < blockCache.length; i++) {
            if (i < tileSlot.connections.size()) {
                TileSlot.Connection connection = tileSlot.connections.get(i);
                if (connection.blockPos != BlockPos.ORIGIN) {
                    IBlockState state = tileSlot.getWorld().getBlockState(connection.blockPos);
                    blockCache[i] = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));

                    continue;
                }
            }

            blockCache[i] = ItemStack.EMPTY;
        }

        if (tileSlot.slot >= 0)
            slot = tileSlot.slot;
        else
            slot = 0;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        addButton(new GuiButtonArrow(BUTTON_SLOT_DOWN, guiLeft + 7, guiTop + 99, 31, 15, GuiButtonArrow.ARROW_DOWN));
        addButton(new GuiButtonArrow(BUTTON_SLOT_UP, guiLeft + 79, guiTop + 99, 31, 15, GuiButtonArrow.ARROW_UP));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                int dx = guiLeft + 9 + x * 21;
                int dy = guiTop + 22 + y * 21;

                if (mouseX >= dx && mouseX <= dx + 18) {
                    if (mouseY >= dy && mouseY <= dy + 18) {
                        ItemStack itemStack = blockCache[x + y * 5];
                        if (!itemStack.isEmpty()) {
                            this.slot = 0;

                            SConfigureSlot packet = new SConfigureSlot();
                            packet.destination = tileSlot.getPos();
                            packet.selection = x + y * 5;
                            packet.slot = 0;

                            PacketHandler.INSTANCE.sendToServer(packet);

                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (tileSlot.inventorySize == 0) {
            slot = 0;
        } else {
            switch (button.id) {
                case BUTTON_SLOT_DOWN: slot--; if (slot < 0) slot = 0; break;
                case BUTTON_SLOT_UP: slot++; if (slot >= tileSlot.inventorySize) slot = tileSlot.inventorySize - 1; break;
            }
        }

        SConfigureSlot packet = new SConfigureSlot();
        packet.destination = tileSlot.getPos();
        packet.selection = -1;
        packet.slot = slot;

        PacketHandler.INSTANCE.sendToServer(packet);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        int mid = (GUI_WIDTH / 2) - fontRendererObj.getStringWidth(Integer.toString(slot)) / 2;
        fontRendererObj.drawString(Integer.toString(slot), guiLeft + mid, guiTop + 105, 4210752);

        super.drawScreen(mouseX, mouseY, partialTicks);

        final RenderItem renderItem = mc.getRenderItem();

        RenderHelper.enableGUIStandardItemLighting();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
                int dx = guiLeft + 9 + x * 21;
                int dy = guiTop + 22 + y * 21;

                ItemStack itemStack = blockCache[x + y * 5];
                if (!itemStack.isEmpty()) {
                    BlockPos pos = tileSlot.connections.get(x + y * 5).blockPos;
                    if (pos == tileSlot.selectedBlock) {
                        drawRect(dx, dy, dx + 18, dy + 18, 0x00FF00FF);
                    }

                    renderItem.renderItemIntoGUI(itemStack, dx, dy);

                    if (mouseX >= dx && mouseX <= dx + 18) {
                        if (mouseY >= dy && mouseY <= dy + 18) {
                            List<String> list = Lists.newArrayList();
                            list.add(itemStack.getDisplayName());
                            list.add("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");

                            drawHoveringText(list, mouseX, mouseY);
                        }
                    }
                }
            }
        }

        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
