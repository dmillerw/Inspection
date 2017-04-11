package me.dmillerw.inspection.block.tile.core;

import com.google.common.collect.Sets;
import me.dmillerw.inspection.block.ModBlocks;
import me.dmillerw.inspection.block.tile.TileCable;
import me.dmillerw.inspection.util.PathFinder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

/**
 * @author dmillerw
 */
public class TileGridOwner extends TileCore implements ITickable {

    public Set<BlockPos> cables = Sets.newHashSet();
    private boolean dirtyState = true;

    @Override
    public void update() {
        if (world != null && !world.isRemote) {
            if (dirtyState) {
                reanalayze();
                dirtyState = false;
                return;
            }

            if (world.getTotalWorldTime() % 2 == 0) {
                for (BlockPos pos : cables) {
                    IBlockState state = world.getBlockState(pos);

                    if (state.getBlock() != ModBlocks.cable) {
                        dirtyState = true;
                        break;
                    }

                    TileCable tile = (TileCable) world.getTileEntity(pos);
                    if (tile == null || tile.pollDirty()) {
                        dirtyState = true;
                        break;
                    }
                }
            }
        }
    }

    public void reanalayze() {
        if (!world.isRemote) {
            PathFinder cableFinder = new PathFinder(world, pos);
            cableFinder.find(true, (pos, face) -> {
                return world.getBlockState(pos).getBlock() == ModBlocks.cable;
            });

            Set<BlockPos> newCables = Sets.newHashSet();
            cableFinder.forEach(newCables::add);

            cables.clear();
            cables.addAll(newCables);
        }
    }
}