package ca.rttv.ecosystemic.mixin.net.minecraft.entity.mob;

import ca.rttv.ecosystemic.mixin.net.minecraft.entity.LivingEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin {
    @Shadow
    @Final
    protected GoalSelector goalSelector;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    protected void ecosystemic$initGoalsTail(CallbackInfo ci) { }

    @Inject(method = "onEatingGrass", at = @At("TAIL"))
    protected void ecosystemic$onEatingGrassTail(CallbackInfo ci) { }
}

