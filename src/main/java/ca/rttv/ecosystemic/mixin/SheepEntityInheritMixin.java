package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SheepEntity.class)
@SuppressWarnings("unused")
abstract class SheepEntityInheritMixin extends AnimalEntityMixin implements AnimalEntityDuck {
    protected SheepEntityInheritMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
}
