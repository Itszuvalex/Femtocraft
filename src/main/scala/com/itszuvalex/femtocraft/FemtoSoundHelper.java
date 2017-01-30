package com.itszuvalex.femtocraft;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * Created by Chris on 1/21/2017.
 */
public class FemtoSoundHelper {
    static int size = 0;

    public static SoundEvent registerSound(String name) {
        ResourceLocation loc = Resources.Sound(name);
        SoundEvent sound = new SoundEvent(loc);
        SoundEvent.REGISTRY.register(size, loc, sound);
        size++;
        return sound;
    }
}
