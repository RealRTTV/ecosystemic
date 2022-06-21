package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PigEntity.class)
public abstract class PigEntityMixin extends PassiveEntity {
    protected PigEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initGoals(CallbackInfo ci) {
        this.goalSelector.add(-1, new EscapeRainGoal(this)); // this works somehow
        this.goalSelector.add(-1, new AvoidRainGoal(this)); // this works somehow
    }
}
