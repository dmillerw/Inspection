package me.dmillerw.inspection.proxy;

import me.dmillerw.inspection.Inspection;
import me.dmillerw.inspection.block.tile.TileCable;
import me.dmillerw.inspection.block.tile.TileSlot;
import me.dmillerw.inspection.lib.ModInfo;
import me.dmillerw.inspection.network.GuiHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author dmillerw
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(TileCable.class, ModInfo.MOD_ID + ":cable");
        GameRegistry.registerTileEntity(TileSlot.class, ModInfo.MOD_ID + ":slot");

        NetworkRegistry.INSTANCE.registerGuiHandler(Inspection.INSTANCE, new GuiHandler());
    }
}
