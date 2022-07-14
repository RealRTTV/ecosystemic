package ca.rttv.ecosystemic.mixin;

import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StatusEffects.class)
abstract class StatusEffectsMixin {
    static {
//        StatusEffectRegistry.init();
    }
}
