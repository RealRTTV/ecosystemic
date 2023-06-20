package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChickenEntity.class)
public abstract class ChickenEntityMixin extends AnimalEntity implements PenDesireDuck, EatingDesireDuck, WaterDesireDuck, LightDesireDuck, DryDesireDuck {
    @Shadow
    public int eggLayTime;

    protected ChickenEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void ecosystemic$initGoalsSuperCall(CallbackInfo ci) {
        super.initGoals();
    }

    @Override
    public List<ModelPart> ecosystemic$headParts(EntityModel<?> model) {
        return List.of(((ChickenEntityModel<?>) model).head, ((ChickenEntityModel<?>) model).beak, ((ChickenEntityModel<?>) model).wattle);
    }

    @Override
    public void ecosystemic$addSleepingTicks(int count) {
        eggLayTime = Math.max(0, eggLayTime - count);
    }

    @Override
    public void ecosystemic$onDrinkWater() {
        ecosystemic$addSleepingTicks(1800);
    }

    @Override
    public float ecosystemic$basePivotY() {
        return 15.0f;
    }

    @Override
    public float ecosystemic$neckMultiplier() {
        return isBaby() ? 2.0f : 5.0f;
    }

    {
        eggLayTime = 24000;
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextInt(I)I"))
    private int eightteenthousand(int original) {
        return 18000;
    }
}
