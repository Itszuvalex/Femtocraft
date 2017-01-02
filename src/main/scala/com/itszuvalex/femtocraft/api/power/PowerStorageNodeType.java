package com.itszuvalex.femtocraft.api.power;

/**
 * Created by Chris on 1/1/2017.
 */
public enum PowerStorageNodeType {
    PRODUCER,
    CONSUMER,
    STORAGE,
    NONE;

    public boolean storesPower() {
        switch (this) {
            case NONE:
                return false;
            default:
                return true;
        }
    }
}
