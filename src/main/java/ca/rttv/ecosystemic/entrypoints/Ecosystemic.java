package ca.rttv.ecosystemic.entrypoints;

import ca.rttv.ecosystemic.network.packet.s2c.play.PenSizeS2CPacket;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetConsumingTimerS2CPacket;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class Ecosystemic implements ModInitializer {
    // todo, add config option for cow milk replenish if a mod already adds it and these two conflict
    // todo, add config option for sheep wool replenish if a mod already adds it and these two conflict
	@Override
	public void onInitialize(ModContainer mod) {
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("ecosystemic", "pens2cpacket"), (client, handler, buf, response) -> new PenSizeS2CPacket(buf).apply(handler));
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("ecosystemic", "resetconsumingtimers2cpacket"), (client, handler, buf, response) -> new ResetConsumingTimerS2CPacket(buf).apply(handler));
	}
}
