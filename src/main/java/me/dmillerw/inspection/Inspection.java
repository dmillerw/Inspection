package me.dmillerw.inspection;

import me.dmillerw.inspection.lib.ModInfo;
import me.dmillerw.inspection.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * @author dmillerw
 */
@Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.MOD_VERSION)
public class Inspection {

    @Mod.Instance(ModInfo.MOD_ID)
    public static Inspection INSTANCE;

    @SidedProxy(
            serverSide = "me.dmillerw.inspection.proxy.CommonProxy",
            clientSide = "me.dmillerw.inspection.proxy.ClientProxy")
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PROXY.preInit(event);
    }
}
