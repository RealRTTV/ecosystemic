package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RabbitEntity.class)
abstract class RabbitEntityMixin extends AnimalEntity implements AnimalEntityDuck {
    protected RabbitEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void ecosystemic$initGoalsSuperCall(CallbackInfo ci) {
        super.initGoals();
    }

    @Override
    public List<ModelPart> ecosystemic$headParts(EntityModel<?> model) {
        return List.of(((RabbitEntityModel<?>) model).head, ((RabbitEntityModel<?>) model).nose, ((RabbitEntityModel<?>) model).leftEar, ((RabbitEntityModel<?>) model).rightEar);
    }

    @Override
    public float ecosystemic$basePivotY() {
        return 16.0f;
    }

    @Override
    public float ecosystemic$neckMultiplier() {
        return 6.0f;
    }
}
