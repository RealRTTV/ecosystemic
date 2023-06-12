package ca.rttv.ecosystemic.duck;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.BlockPos;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public interface AnimalEntityDuck {

    @ClientOnly
    void ecosystemic$visitedSpaceCount(int count);

    @ClientOnly
    int ecosystemic$visitedSpaceCount();

    int ecosystemic$ticksWithSkylight();

    List<ModelPart> ecosystemic$headParts(EntityModel<?> model); // todo, remove cause its unnecessary

    float ecosystemic$headAngle(float tickDelta);

    float ecosystemic$neckAngle(float tickDelta);

    void ecosystemic$waterTimer(int value);

    float ecosystemic$basePivotY();

    float ecosystemic$neckMultiplier();

    void ecosystemic$onDrinkWater(IntSupplier drinkableWaterBlocks);

    Set<BlockPos> ecosystemic$visitedSpaces();

    void ecosystemic$addSleepingTicks(int count);

    boolean ecosystemic$addPitch();
}
