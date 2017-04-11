package me.dmillerw.inspection.proxy;

import me.dmillerw.inspection.block.tile.TileSlot;
import me.dmillerw.inspection.client.model.BaseModelLoader;
import me.dmillerw.inspection.client.render.RenderTileSlot;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author dmillerw
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ModelLoaderRegistry.registerLoader(new BaseModelLoader());

        ClientRegistry.bindTileEntitySpecialRenderer(TileSlot.class, new RenderTileSlot());
    }
}
