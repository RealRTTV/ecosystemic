package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RabbitEntity.class)
abstract class RabbitEntityMixin implements AnimalEntityDuck { }
