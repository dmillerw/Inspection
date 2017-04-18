package me.dmillerw.inspection.item;

import me.dmillerw.inspection.lib.ModInfo;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author dmillerw
 */
@GameRegistry.ObjectHolder(ModInfo.MOD_ID)
public class ModItems {

    public static final ItemConfigurator configurator = null;

    @Mod.EventBusSubscriber
    public static class Loader {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                    new ItemConfigurator().setRegistryName(ModInfo.MOD_ID, "configurator")
            );
        }
    }
}
