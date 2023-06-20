package ca.rttv.ecosystemic.duck;

import net.minecraft.entity.Entity;

public interface ServerWorldDuck {
    void ecosystemic$sendPenSizePacket(Entity entity, PenDesireDuck penDesireDuck);
}
