package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.StreamSupport;

@Mixin(CowEntity.class)
public abstract class CowEntityMixin extends PassiveEntity {
    protected CowEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initGoals(CallbackInfo ci) {
        this.goalSelector.add(-1, new EscapeRainGoal(this)); // this works somehow
        this.goalSelector.add(-1, new AvoidRainGoal(this)); // this works somehow
    }
}
