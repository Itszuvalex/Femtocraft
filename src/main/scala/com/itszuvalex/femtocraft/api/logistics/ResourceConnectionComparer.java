package com.itszuvalex.femtocraft.api.logistics;

import java.util.Comparator;

/**
 * Created by Chris on 2/20/2017.
 */
public class ResourceConnectionComparer {

    public static <T> Comparator<IConnection<T>> FromResource(final IResource<T> resource) {
        return new Comparator<IConnection<T>>() {
            @Override
            public int compare(IConnection<T> o1, IConnection<T> o2) {
                return resource.sort(o1.buffer(), o2.buffer()) ? -1 : 0;
            }
        };
    }
}
