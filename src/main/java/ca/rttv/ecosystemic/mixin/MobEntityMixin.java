package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(MobEntity.class)
abstract class MobEntityMixin extends LivingEntityMixin {
    @Shadow
    @Final
    protected GoalSelector goalSelector;

    @Shadow
    public abstract EntityNavigation getNavigation();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    protected void ecosystemic$initGoalsTail(CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        goalSelector.add(-1, new EscapeRainGoal((PathAwareEntity) (Object) this)); // this works somehow
        goalSelector.add(-1, new AvoidRainGoal((PathAwareEntity) (Object) this)); // this works somehow
    }
}

