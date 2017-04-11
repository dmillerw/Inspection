package me.dmillerw.inspection.util;

import com.google.common.collect.Sets;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author dmillerw
 */
public class PathFinder {

    private World world;
    private BlockPos start;

    private Set<BlockPos> connectedBlocks;

    public PathFinder(World world, BlockPos start) {
        this.world = world;
        this.start = start;
        this.connectedBlocks = Sets.newHashSet();
    }

    public PathFinder find(boolean ignoreStart, BiFunction<? super BlockPos, EnumFacing, Boolean> function) {
        if (!ignoreStart) {
            if (!function.apply(start, null))
                return this;

            connectedBlocks.add(start);
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            find(start.offset(facing), facing.getOpposite(), function);
        }

        return this;
    }

    private void find(BlockPos pos, EnumFacing reverse, BiFunction<? super BlockPos, EnumFacing, Boolean> function) {
        if (!function.apply(pos, reverse)) {
            return;
        }

        if (!connectedBlocks.contains(pos)) {
            connectedBlocks.add(pos);
            for (EnumFacing facing : EnumFacing.VALUES) {
                find(pos.offset(facing), facing.getOpposite(), function);
            }
        }

        return;
    }

    public Set<BlockPos> getConnectedBlocks() {
        return connectedBlocks;
    }

    public void forEach(Consumer<? super BlockPos> consumer) {
        connectedBlocks.forEach(consumer);
    }
}