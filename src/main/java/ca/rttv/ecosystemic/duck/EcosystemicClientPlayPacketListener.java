package ca.rttv.ecosystemic.duck;

import ca.rttv.ecosystemic.network.packet.s2c.play.PenSizeS2CPacket;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetConsumingTimerS2CPacket;

public interface EcosystemicClientPlayPacketListener {
    void ecosystemic$onPenSize(PenSizeS2CPacket penSizeS2CPacket);

    void ecosystemic$onResetConsumingTimer(ResetConsumingTimerS2CPacket resetWaterTimerS2CPacket);
}
