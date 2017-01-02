package com.itszuvalex.femtocraft.api.power;

/**
 * Created by Chris on 1/1/2017.
 */
public enum PowerConnectionNodeType {
    MAIN,
    LEAF;

    public boolean canConnect(PowerConnectionNodeType type) {
        switch (this) {
            case MAIN:
                return true;
            case LEAF:
                if (type == MAIN) return true;
            default:
                return false;
        }
    }
}
