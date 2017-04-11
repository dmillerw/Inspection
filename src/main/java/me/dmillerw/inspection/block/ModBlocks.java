package me.dmillerw.inspection.block;

import me.dmillerw.inspection.lib.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * @author dmillerw
 */
@GameRegistry.ObjectHolder(ModInfo.MOD_ID)
public class ModBlocks {

    public static final BlockCable cable = null;
    @GameRegistry.ObjectHolder(ModInfo.MOD_ID + ":cable")
    public static final ItemBlock cable_item = null;

    public static final BlockSlot slot = null;
    @GameRegistry.ObjectHolder(ModInfo.MOD_ID + ":slot")
    public static final ItemBlock slot_item = null;

    @Mod.EventBusSubscriber
    public static class Loader {

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(
                    new BlockCable().setRegistryName(ModInfo.MOD_ID, "cable"),
                    new BlockSlot().setRegistryName(ModInfo.MOD_ID, "slot")
            );
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(
                    new ItemBlock(ModBlocks.cable).setRegistryName(ModInfo.MOD_ID, "cable"),
                    new ItemBlock(ModBlocks.slot).setRegistryName(ModInfo.MOD_ID, "slot")
            );
        }
    }
}
