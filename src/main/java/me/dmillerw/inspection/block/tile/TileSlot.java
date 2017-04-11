package me.dmillerw.inspection.block.tile;

import com.google.common.collect.Lists;
import me.dmillerw.inspection.block.ModBlocks;
import me.dmillerw.inspection.block.tile.core.TileGridOwner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

/**
 * @author dmillerw
 */
public class TileSlot extends TileGridOwner implements ITickable {

    public static class Connection {

        public BlockPos blockPos;
        public EnumFacing side;

        public Connection(BlockPos blockPos, EnumFacing side) {
            this.blockPos = blockPos;
            this.side = side;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;

            Connection that = (Connection) object;

            return blockPos.equals(that.blockPos);
        }

        @Override
        public int hashCode() {
            return blockPos.hashCode();
        }
    }

    public List<Connection> connections = Lists.newArrayList();
    public BlockPos selectedBlock = BlockPos.ORIGIN;
    private IItemHandler itemHandler;
    public int slot = -1;

    public ItemStack renderItem = ItemStack.EMPTY;
    public int inventorySize = 0;

    @Override
    public void writeToDisk(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Connection connection : connections) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("position", connection.blockPos.toLong());
            tag.setInteger("side", connection.side.ordinal());
            list.appendTag(tag);
        }
        compound.setTag("connections", list);


        compound.setLong("selectedBlock", selectedBlock.toLong());
        compound.setInteger("slot", slot);

        NBTTagCompound item = new NBTTagCompound();
        renderItem.writeToNBT(item);
        compound.setTag("item", item);

        compound.setInteger("inventorySize", inventorySize);
    }

    @Override
    public void readFromDisk(NBTTagCompound compound) {
        connections.clear();
        NBTTagList list = compound.getTagList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i=0; i<list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            connections.add(new Connection(
                    BlockPos.fromLong(tag.getLong("position")),
                    EnumFacing.VALUES[tag.getInteger("side")]
            ));
        }

        selectedBlock = BlockPos.fromLong(compound.getLong("selectedBlock"));
        slot = compound.getInteger("slot");
        renderItem = new ItemStack(compound.getCompoundTag("item"));
        inventorySize = compound.getInteger("inventorySize");
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            if (selectedBlock != BlockPos.ORIGIN && itemHandler != null && slot >= 0) {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!ItemStack.areItemStacksEqual(stack, renderItem)) {
                    renderItem = stack.copy();

                    markDirtyAndNotify();
                }
            }
        }
    }

    public void handleUpdate(int selection, int slot) {
        if (selection >= 0) {
            if (selection >= connections.size() || connections.get(selection) == null) {
                this.selectedBlock = BlockPos.ORIGIN;
                this.itemHandler = null;
                this.renderItem = ItemStack.EMPTY;
                this.inventorySize = 0;
                this.slot = -1;
            } else {
                Connection connection = connections.get(selection);

                this.selectedBlock = connection.blockPos;
                this.itemHandler = world.getTileEntity(selectedBlock).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, connection.side);
                this.inventorySize = itemHandler.getSlots();
                this.slot = slot;
            }
        }

        if (slot >= 0) this.slot = slot;

        markDirtyAndNotify();
    }

    @Override
    public void reanalayze() {
        super.reanalayze();

        connections.clear();
        this.cables.forEach(this::addConnections);
        this.addConnections(getPos());

        boolean foundSelected = false;
        for (Connection connection : connections) {
            if (connection.blockPos.equals(selectedBlock)) {
                foundSelected = true;
                break;
            }
        }

        if (!foundSelected) {
            this.selectedBlock = BlockPos.ORIGIN;
            this.itemHandler = null;
            this.renderItem = ItemStack.EMPTY;
            this.inventorySize = 0;
            this.slot = -1;
        }

        markDirtyAndNotify();
    }

    private void addConnections(BlockPos origin) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos pos = origin.offset(facing);
            IBlockState state = world.getBlockState(pos);
            Connection connection = new Connection(pos, facing.getOpposite());

            if (!connections.contains(connection) && state.getBlock() != blockType && state.getBlock() != ModBlocks.cable && world.getTileEntity(pos) != null) {
                TileEntity tile = world.getTileEntity(pos);
                if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
                    connections.add(connection);
                }
            }
        }
    }
}
