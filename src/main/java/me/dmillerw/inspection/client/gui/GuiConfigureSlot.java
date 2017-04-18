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
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.List;

/**
 * @author dmillerw
 */
public class GuiConfigureSlot extends GuiScreen {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ModInfo.MOD_ID, "textures/gui/slot_debug.png");

    private static final int BUTTON_CONTAINER_DOWN = 0;
    private static final int BUTTON_CONTAINER_UP = 1;
    private static final int BUTTON_SLOT_DOWN = 2;
    private static final int BUTTON_SLOT_UP = 3;

    private static final int CONTAINER_SLOT_COLUMNS = 5;
    private static final int CONTAINER_SLOT_ROWS = 3;
    private static final int CONTAINER_SLOT_POS_X = 9;
    private static final int CONTAINER_SLOT_POS_Y = 22;

    private static final int SLOTS_SLOT_COLUMNS = 5;
    private static final int SLOTS_SLOT_ROWS = 2;
    private static final int SLOTS_SLOT_POS_X = 9;
    private static final int SLOTS_SLOT_POS_Y = 100;

    private static final int SLOT_SPACING = 21;
    private static final int SLOT_SIZE = 16;

    public static final int GUI_WIDTH = 132;
    public static final int GUI_HEIGHT = 145;

    private int guiLeft;
    private int guiTop;

    private TileSlot tileSlot;
    private ItemStack[] connectionCache;

    private ItemStack connectionCache(int index) {
        if (index < 0 || index >= connectionCache.length)
            return ItemStack.EMPTY;
        else
            return connectionCache[index];
    }

    private int containerOffset = 0;
    private int slotOffset = 0;

    public GuiConfigureSlot(TileSlot tileSlot) {
        this.tileSlot = tileSlot;

        this.connectionCache = new ItemStack[15];
        for (int i = 0; i < connectionCache.length; i++) {
            if (i < tileSlot.connections.size()) {
                TileSlot.Connection connection = tileSlot.connections.get(i);
                if (connection.blockPos != BlockPos.ORIGIN) {
                    IBlockState state = tileSlot.getWorld().getBlockState(connection.blockPos);
                    connectionCache[i] = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));

                    continue;
                }
            }

            connectionCache[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        addButton(new GuiButtonArrow(BUTTON_CONTAINER_DOWN, guiLeft + 114, guiTop + 64, 11, 16, GuiButtonArrow.ARROW_DOWN));
        addButton(new GuiButtonArrow(BUTTON_CONTAINER_UP, guiLeft + 114, guiTop + 22, 11, 16, GuiButtonArrow.ARROW_UP));
        addButton(new GuiButtonArrow(BUTTON_SLOT_DOWN, guiLeft + 114, guiTop + 121, 11, 16, GuiButtonArrow.ARROW_DOWN));
        addButton(new GuiButtonArrow(BUTTON_SLOT_UP, guiLeft + 114, guiTop + 100, 11, 16, GuiButtonArrow.ARROW_UP));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (int y = 0; y < CONTAINER_SLOT_ROWS; y++) {
            for (int x = 0; x < CONTAINER_SLOT_COLUMNS; x++) {
                int index = (x + y * CONTAINER_SLOT_COLUMNS);
                int dx = guiLeft + CONTAINER_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + CONTAINER_SLOT_POS_Y + y * SLOT_SPACING;

                if (mouseX >= dx && mouseX <= dx + SLOT_SIZE) {
                    if (mouseY >= dy && mouseY <= dy + SLOT_SIZE) {
                        SConfigureSlot packet = new SConfigureSlot();
                        packet.destination = tileSlot.getPos();
                        packet.setSelectionIndex(index);
                        packet.setSlot(0);

                        PacketHandler.INSTANCE.sendToServer(packet);

                        return;
                    }
                }
            }
        }

        for (int y = 0; y < SLOTS_SLOT_ROWS; y++) {
            for (int x = 0; x < SLOTS_SLOT_COLUMNS; x++) {
                int index = x + (y + slotOffset) * SLOTS_SLOT_COLUMNS;
                int dx = guiLeft + SLOTS_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + SLOTS_SLOT_POS_Y + y * SLOT_SPACING;

                ItemStack itemStack = tileSlot.getItemStack(index);
                if (!itemStack.isEmpty()) {
                    if (mouseX >= dx && mouseX <= dx + SLOT_SIZE) {
                        if (mouseY >= dy && mouseY <= dy + SLOT_SIZE) {
                            SConfigureSlot packet = new SConfigureSlot();
                            packet.destination = tileSlot.getPos();
                            packet.setSlot(index);

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
        switch (button.id) {
            case BUTTON_SLOT_DOWN:
                slotOffset++;
                break;
            case BUTTON_SLOT_UP:
                slotOffset--;
                if (slotOffset < 0) slotOffset = 0;
                break;
        }
        /*if (tileSlot.inventorySize == 0) {
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

        PacketHandler.INSTANCE.sendToServer(packet);*/
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        super.drawScreen(mouseX, mouseY, partialTicks);

        final RenderItem renderItem = mc.getRenderItem();

        RenderHelper.enableGUIStandardItemLighting();

        // Item Rendering
        for (int y = 0; y < CONTAINER_SLOT_ROWS; y++) {
            for (int x = 0; x < CONTAINER_SLOT_COLUMNS; x++) {
                int index = x + y * CONTAINER_SLOT_COLUMNS;
                int dx = guiLeft + CONTAINER_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + CONTAINER_SLOT_POS_Y + y * SLOT_SPACING;

                ItemStack itemStack = connectionCache[index];
                if (!itemStack.isEmpty()) {
                    TileSlot.Connection connection = tileSlot.connections.get(index);
                    if (connection.blockPos.equals(tileSlot.selectedBlock)) {
                        drawRect(dx, dy, dx + SLOT_SIZE, dy + SLOT_SIZE, 0xFFFF0000);
                    }

                    renderItem.renderItemIntoGUI(itemStack, dx, dy);
                }
            }
        }

        for (int y = 0; y < SLOTS_SLOT_ROWS; y++) {
            for (int x = 0; x < SLOTS_SLOT_COLUMNS; x++) {
                int index = x + (y + slotOffset) * SLOTS_SLOT_COLUMNS;
                int dx = guiLeft + SLOTS_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + SLOTS_SLOT_POS_Y + y * SLOT_SPACING;

                ItemStack itemStack = tileSlot.getItemStack(index);
                if (!itemStack.isEmpty()) {
                    renderItem.renderItemIntoGUI(itemStack, dx, dy);
                }
            }
        }

        RenderHelper.disableStandardItemLighting();

        // Tooltips
        for (int y = 0; y < CONTAINER_SLOT_ROWS; y++) {
            for (int x = 0; x < CONTAINER_SLOT_COLUMNS; x++) {
                int index = x + y * CONTAINER_SLOT_COLUMNS;
                int dx = guiLeft + CONTAINER_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + CONTAINER_SLOT_POS_Y + y * SLOT_SPACING;

                ItemStack itemStack = connectionCache(index);
                if (!itemStack.isEmpty()) {
                    TileSlot.Connection connection = tileSlot.connections.get(index);
                    if (mouseX >= dx && mouseX <= dx + SLOT_SIZE) {
                        if (mouseY >= dy && mouseY <= dy + SLOT_SIZE) {
                            List<String> list = Lists.newArrayList();
                            list.add(itemStack.getDisplayName());
                            list.add("(" + connection.blockPos.getX() + ", " + connection.blockPos.getY() + ", " + connection.blockPos.getZ() + ")");
                            list.add("Side: " + connection.side);

                            drawHoveringText(list, mouseX, mouseY);
                        }
                    }
                }
            }
        }

        for (int y = 0; y < SLOTS_SLOT_ROWS; y++) {
            for (int x = 0; x < SLOTS_SLOT_COLUMNS; x++) {
                int index = x + (y + slotOffset) * SLOTS_SLOT_COLUMNS;
                int dx = guiLeft + SLOTS_SLOT_POS_X + x * SLOT_SPACING;
                int dy = guiTop + SLOTS_SLOT_POS_Y + y * SLOT_SPACING;

                ItemStack itemStack = tileSlot.getItemStack(index);
                if (!itemStack.isEmpty()) {
                    if (mouseX >= dx && mouseX <= dx + SLOT_SIZE) {
                        if (mouseY >= dy && mouseY <= dy + SLOT_SIZE) {
                            List<String> list = Lists.newArrayList();
                            list.add(itemStack.getDisplayName());
                            list.add(TextFormatting.ITALIC + "Slot " + index);

                            drawHoveringText(list, mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
