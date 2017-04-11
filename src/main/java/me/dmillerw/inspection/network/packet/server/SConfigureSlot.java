package me.dmillerw.inspection.network.packet.server;

import io.netty.buffer.ByteBuf;
import me.dmillerw.inspection.block.tile.TileSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author dmillerw
 */
public class SConfigureSlot implements IMessage {

    public BlockPos destination;
    public int selection;
    public int slot;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(destination.toLong());
        buf.writeInt(selection);
        buf.writeInt(slot);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        destination = BlockPos.fromLong(buf.readLong());
        selection = buf.readInt();
        slot = buf.readInt();
    }

    public static class Handler implements IMessageHandler<SConfigureSlot, IMessage> {

        @Override
        public IMessage onMessage(SConfigureSlot message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                TileSlot tile = (TileSlot) ctx.getServerHandler().playerEntity.world.getTileEntity(message.destination);
                if (tile != null) tile.handleUpdate(message.selection, message.slot);
            });
            return null;
        }
    }
}
