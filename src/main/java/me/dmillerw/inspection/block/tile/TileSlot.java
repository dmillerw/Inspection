package me.dmillerw.inspection.block.tile;

import com.google.common.collect.Lists;
import me.dmillerw.inspection.block.ModBlocks;
import me.dmillerw.inspection.block.tile.core.TileGridOwner;
import me.dmillerw.inspection.network.packet.server.SConfigureSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
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

            if (!blockPos.equals(that.blockPos)) return false;
            return side == that.side;
        }

        @Override
        public int hashCode() {
            int result = blockPos.hashCode();
            result = 31 * result + side.hashCode();
            return result;
        }
    }

    public List<Connection> connections = Lists.newArrayList();
    public BlockPos selectedBlock = BlockPos.ORIGIN;
    private IItemHandler itemHandler;
    public int slot = -1;

    private NonNullList<ItemStack> inventoryContents = NonNullList.withSize(1, ItemStack.EMPTY);

    public ItemStack getItemStack(int index) {
        if (index < 0 || index >= inventoryContents.size())
            return ItemStack.EMPTY;

        return inventoryContents.get(index);
    }

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

        NBTTagList contents = new NBTTagList();
        for (ItemStack itemStack : inventoryContents) {
            NBTTagCompound tag = new NBTTagCompound();
            itemStack.writeToNBT(tag);
            contents.appendTag(tag);
        }
        compound.setTag("inventoryContents", contents);
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

        NBTTagList contents = compound.getTagList("inventoryContents", Constants.NBT.TAG_COMPOUND);
        inventoryContents = NonNullList.withSize(contents.tagCount(), ItemStack.EMPTY);

        for (int i=0; i<contents.tagCount(); i++) {
            NBTTagCompound tag = contents.getCompoundTagAt(i);
            inventoryContents.set(i, new ItemStack(tag));
        }
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            boolean update = false;
            if (selectedBlock != BlockPos.ORIGIN && itemHandler != null && slot >= 0) {
                if (inventoryContents.size() != itemHandler.getSlots()) {
                    rebuildItemContents();
                    update = true;
                }

                for (int i=0; i<inventoryContents.size(); i++) {
                    ItemStack ours = inventoryContents.get(i);
                    ItemStack theirs = itemHandler.getStackInSlot(i);

                    if (!ItemStack.areItemsEqual(ours, theirs)) {
                        update = true;
                        inventoryContents.set(i, theirs);
                    }
                }
            }

            if (update)
                markDirtyAndNotify();
        }
    }

    public void handleLeftClick(EntityPlayer player) {
        if (world.isRemote) return;
        if (itemHandler == null) return;

        ItemStack droppedStack = ItemStack.EMPTY;
        if (player.isSneaking())
            droppedStack = itemHandler.extractItem(slot, 1, false);
        else
            droppedStack = itemHandler.extractItem(slot, 64, false);

        if (!droppedStack.isEmpty())
            InventoryHelper.spawnItemStack(world, player.posX, player.posY, player.posZ, droppedStack);

        markDirtyAndNotify();
    }

    public void handleRightClick(EntityPlayer player, EnumHand hand, EnumFacing facing) {
        if (world.isRemote) return;
        if (itemHandler == null) return;

        ItemStack held = player.getHeldItem(hand).copy();
        player.setHeldItem(hand, itemHandler.insertItem(slot, held, false));

        markDirtyAndNotify();
    }

    public void handleUpdate(SConfigureSlot updatePacket) {
        if (updatePacket.hasSelection) {
            if (updatePacket.selection >= 0) {
                if (updatePacket.selection >= connections.size() || connections.get(updatePacket.selection) == null) {
                    this.selectedBlock = BlockPos.ORIGIN;
                    this.itemHandler = null;
                    this.slot = -1;
                } else {
                    Connection connection = connections.get(updatePacket.selection);

                    this.selectedBlock = connection.blockPos;
                    this.itemHandler = world.getTileEntity(selectedBlock).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, connection.side);
                    if (updatePacket.hasSlot) this.slot = updatePacket.slot;
                }
            } else {
                this.selectedBlock = BlockPos.ORIGIN;
                this.itemHandler = null;
                this.slot = -1;
            }
        }

        if (updatePacket.hasSlot)
            if (slot >= 0) this.slot = updatePacket.slot;

        rebuildItemContents();
        markDirtyAndNotify();
    }

    private void rebuildItemContents() {
        if (itemHandler == null) {
            inventoryContents = NonNullList.withSize(1, ItemStack.EMPTY);
        } else {
            inventoryContents = NonNullList.withSize(itemHandler.getSlots(), ItemStack.EMPTY);
            for (int i=0; i<inventoryContents.size(); i++) {
                inventoryContents.set(i, itemHandler.getStackInSlot(i));
            }
        }
    }

    @Override
    public void reanalayze() {
        super.reanalayze();

        connections.clear();
        this.trackedLocations.forEach(this::addConnections);
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
            this.slot = -1;
        }

        rebuildItemContents();
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
