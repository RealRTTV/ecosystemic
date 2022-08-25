package ca.rttv.ecosystemic.duck;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public interface AnimalEntityDuck {

    @Environment(EnvType.CLIENT)
    void ecosystemic$visitedSpaceCount(int count);

    @Environment(EnvType.CLIENT)
    int ecosystemic$visitedSpaceCount();

    int ecosystemic$ticksWithSkylight();

    List<ModelPart> ecosystemic$headParts(EntityModel<?> model);

    float ecosystemic$headAngle(float tickDelta);

    float ecosystemic$neckAngle(float tickDelta);

    void ecosystemic$waterTimer(int value);

    float ecosystemic$basePivotY();

    float ecosystemic$neckMultiplier();

    void ecosystemic$onDrinkWater(IntSupplier drinkableWaterBlocks);

    Set<BlockPos> ecosystemic$visitedSpaces();
}
