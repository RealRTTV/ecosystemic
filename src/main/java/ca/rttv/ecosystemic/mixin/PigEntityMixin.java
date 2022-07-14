package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.passive.PigEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PigEntity.class)
abstract class PigEntityMixin implements AnimalEntityDuck { }
