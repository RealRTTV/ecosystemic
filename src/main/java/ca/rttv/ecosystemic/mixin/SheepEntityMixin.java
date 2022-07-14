package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SheepEntity.class)
abstract class SheepEntityMixin implements AnimalEntityDuck { }
