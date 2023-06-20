package ca.rttv.ecosystemic.duck;

import ca.rttv.ecosystemic.Nibble;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;

import java.util.List;

public interface ConsumingDesireDuck extends PenDesireDuck {
    List<ModelPart> ecosystemic$headParts(EntityModel<?> model); // todo, remove cause its unnecessary, wait it is???

    float ecosystemic$headAngle(float tickDelta);

    float ecosystemic$neckAngle(float tickDelta);

    float ecosystemic$basePivotY();

    default float ecosystemic$headMultiplier() {
        return 1.0f;
    }

    float ecosystemic$neckMultiplier();

    boolean ecosystemic$addPitch();

    void ecosystemic$timer(int timer, Nibble type);

    void ecosystemic$onConsume(boolean success, Nibble type);
}
