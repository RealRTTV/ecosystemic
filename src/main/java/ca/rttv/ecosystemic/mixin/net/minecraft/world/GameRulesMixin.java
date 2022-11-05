package ca.rttv.ecosystemic.mixin.net.minecraft.world;

import ca.rttv.ecosystemic.registry.GameRulesRegistry;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRules.class)
abstract class GameRulesMixin {
    static {
        GameRulesRegistry.init();
    }
}
