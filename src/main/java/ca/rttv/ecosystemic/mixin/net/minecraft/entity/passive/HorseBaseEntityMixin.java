package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseBaseEntity.class)
public abstract class HorseBaseEntityMixin extends AnimalEntity implements PenDesireDuck, WaterDesireDuck, EatingDesireDuck, LightDesireDuck, DryDesireDuck {
    @Unique
    private static double firstHorsePenSize;
    @Unique
    private static double secondHorsePenSize;

    protected HorseBaseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    protected void initGoals(CallbackInfo ci) {
        super.initGoals();
    }

    @Inject(method = "method_49124", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/HorseBaseEntity;method_49123(DDDDLnet/minecraft/util/random/RandomGenerator;)D"))
    private void method_49124(PassiveEntity passiveEntity, HorseBaseEntity horseBaseEntity, EntityAttribute entityAttribute, double d, double e, CallbackInfo ci) {
        firstHorsePenSize = ecosystemic$penSize();
        secondHorsePenSize = ((PenDesireDuck) horseBaseEntity).ecosystemic$penSize();
    }

    @Redirect(method = "method_49123", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextDouble()D", ordinal = 1))
    private static double firstHorseBonus(RandomGenerator instance) {
        return 1.0d - 1.0d / (0.1d * firstHorsePenSize + 1.0d);
    }

    @Redirect(method = "method_49123", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextDouble()D", ordinal = 2))
    private static double secondHorseBonus(RandomGenerator instance) {
        return 1.0d - 1.0d / (0.1d * secondHorsePenSize + 1.0d);
    }
}
