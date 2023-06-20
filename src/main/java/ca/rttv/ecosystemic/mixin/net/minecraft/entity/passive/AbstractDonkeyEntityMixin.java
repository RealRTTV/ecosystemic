package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.ConsumingDesireDuck;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(AbstractDonkeyEntity.class)
public abstract class AbstractDonkeyEntityMixin extends AnimalEntity implements ConsumingDesireDuck { // todo, it looks awful
    protected AbstractDonkeyEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
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
        return isBaby() ? 7.0f : 9.0f;
    }

    @Override
    public boolean ecosystemic$addPitch() {
        return true;
    }
}
