package ca.rttv.ecosystemic.duck;

import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;

public interface EcosystemicClientPlayPacketListener {
    void onVisitedSpaceCount(VisitedSpaceCountS2CPacket visitedSpaceCountS2CPacket);
}
