package me.dmillerw.inspection.block.tile;

import me.dmillerw.inspection.block.BlockCable;
import me.dmillerw.inspection.block.ModBlocks;
import me.dmillerw.inspection.block.property.ConnectionType;
import me.dmillerw.inspection.block.tile.core.TileCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.Arrays;

/**
 * @author dmillerw
 */
public class TileCable extends TileCore {

    private ConnectionType[] connectionMap = new ConnectionType[6];
    private boolean dirty = false;

    public TileCable() {
        Arrays.fill(connectionMap, ConnectionType.NONE);
    }

    @Override
    public void writeToDisk(NBTTagCompound tag) {
        for (int i = 0; i < connectionMap.length; i++) {
            tag.setInteger("connectionMap#" + i, connectionMap[i].ordinal());
        }
    }

    @Override
    public void readFromDisk(NBTTagCompound compound) {
        for (int i = 0; i < connectionMap.length; i++) {
            connectionMap[i] = ConnectionType.getValues()[compound.getInteger("connectionMap#" + i)];
        }
    }

    public void updateState() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            final IBlockState state = world.getBlockState(pos.offset(facing));

            if (state.getBlock() == ModBlocks.cable) {
                connectionMap[facing.ordinal()] = ConnectionType.CABLE;
            } else if (world.getTileEntity(pos.offset(facing)) != null) {
                connectionMap[facing.ordinal()] = ConnectionType.BLOCK;
            } else {
                connectionMap[facing.ordinal()] = ConnectionType.NONE;
            }
        }

        markDirtyAndNotify();
    }

    public boolean pollDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    public void markForGridUpdate() {
        this.dirty = true;
    }

    public boolean isConnected(EnumFacing facing) {
        return getConnectionType(facing) != ConnectionType.NONE;
    }

    public ConnectionType getConnectionType(EnumFacing facing) {
        return connectionMap[facing.ordinal()];
    }

    public IBlockState getRenderBlockstate(IBlockState state) {
        IExtendedBlockState eState = (IExtendedBlockState) state;

        for (int i = 0; i < connectionMap.length; i++) {
            eState = eState.withProperty(BlockCable.PROPERTIES[i], connectionMap[i]);
        }

        return eState;
    }
}