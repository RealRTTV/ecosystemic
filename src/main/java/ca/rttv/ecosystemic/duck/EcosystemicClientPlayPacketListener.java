package ca.rttv.ecosystemic.duck;

import ca.rttv.ecosystemic.network.packet.s2c.play.ResetWaterTimerS2CPacket;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;

public interface EcosystemicClientPlayPacketListener {
    void ecosystemic$onVisitedSpaceCount(VisitedSpaceCountS2CPacket visitedSpaceCountS2CPacket);

    void ecosystemic$onResetWaterTimer(ResetWaterTimerS2CPacket resetWaterTimerS2CPacket);
}
