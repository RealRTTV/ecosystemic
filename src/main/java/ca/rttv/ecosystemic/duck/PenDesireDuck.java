package ca.rttv.ecosystemic.duck;

import net.minecraft.util.math.BlockPos;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.Set;

public interface PenDesireDuck { // todo, make checks not be >= 12 but instead be dynamic, with a hard cap really high
    @ClientOnly
    void ecosystemic$penSize(int count);

    @ClientOnly
    int ecosystemic$penSize();

    Set<BlockPos> ecosystemic$pen();
}
