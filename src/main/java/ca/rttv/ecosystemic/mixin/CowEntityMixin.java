package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.passive.CowEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CowEntity.class)
abstract class CowEntityMixin implements AnimalEntityDuck { }
