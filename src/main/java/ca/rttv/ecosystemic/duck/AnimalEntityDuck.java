package ca.rttv.ecosystemic.duck;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface AnimalEntityDuck {

    @Environment(EnvType.CLIENT)
    void ecosystemic$visitedSpaceCount(int count);

    @Environment(EnvType.CLIENT)
    int ecosystemic$visitedSpaceCount();

    int ecosystemic$ticksWithSkylight();
}
