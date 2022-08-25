package ca.rttv.ecosystemic.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CowEntity.class)
@SuppressWarnings("unused")
abstract class CowEntityInheritMixin extends AnimalEntityMixin {
    protected CowEntityInheritMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }
}
