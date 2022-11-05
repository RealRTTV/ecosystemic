package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(HorseEntity.class)
public abstract class HorseEntityMixin extends AnimalEntity implements AnimalEntityDuck { // todo, it looks awful
    protected HorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public List<ModelPart> ecosystemic$headParts(EntityModel<?> model) {
        return List.of(((HorseEntityModel<?>) model).head);
    }

    @Override
    public float ecosystemic$basePivotY() {
        return 4.0f;
    }

    @Override
    public float ecosystemic$neckMultiplier() {
        return 3.5f;
    }

    @Override
    public boolean ecosystemic$addPitch() {
        return true;
    }
}
