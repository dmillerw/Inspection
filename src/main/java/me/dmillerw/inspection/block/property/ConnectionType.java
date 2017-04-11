package me.dmillerw.inspection.block.property;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * @author dmillerw
 */
public enum ConnectionType implements IStringSerializable {

    NONE("none"),
    CABLE("cable"),
    BLOCK("block");

    private String name;
    private ConnectionType(String name) {
        this.name = name;
    }

    public boolean renderCable() {
        return this == CABLE || this == BLOCK;
    }

    public boolean renderConnector() {
        return this == BLOCK;
    }

    @Override
    public String getName() {
        return this.name;
    }

    private static ConnectionType[] values;
    public static ConnectionType[] getValues() {
        if (values == null)
            values = values();
        return values;
    }

    public static class Property implements IUnlistedProperty<ConnectionType> {

        private String name;
        public Property(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isValid(ConnectionType value) {
            return true;
        }

        @Override
        public Class<ConnectionType> getType() {
            return ConnectionType.class;
        }

        @Override
        public String valueToString(ConnectionType value) {
            return value.name;
        }
    }
}