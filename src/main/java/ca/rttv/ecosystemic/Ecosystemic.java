package ca.rttv.ecosystemic;

import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class Ecosystemic implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("ecosystemic", "visitedspacecounts2cpacket"), (client, handler, buf, response) -> new VisitedSpaceCountS2CPacket(buf).apply(handler));
	}
}
