package me.dmillerw.inspection.item;

import me.dmillerw.inspection.lib.ModInfo;
import net.minecraft.item.Item;

/**
 * @author dmillerw
 */
public class ItemConfigurator extends Item {

    public ItemConfigurator() {
        super();

        setMaxStackSize(1);
        setMaxDamage(0);

        setUnlocalizedName(ModInfo.MOD_ID + ":configurator");
    }
}
