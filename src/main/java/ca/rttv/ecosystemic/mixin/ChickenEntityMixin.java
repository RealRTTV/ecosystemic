package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.passive.ChickenEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChickenEntity.class)
abstract class ChickenEntityMixin implements AnimalEntityDuck { }
