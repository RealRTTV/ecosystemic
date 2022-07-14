package ca.rttv.ecosystemic.registry;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;

// unused
public interface StatusEffectRegistry {
    Object FIX_CLASS_LOADING_ORDER_ERROR = StatusEffects.class;

    LinkedHashMap<Identifier, StatusEffect> ENTRIES = new LinkedHashMap<>(1);

    static StatusEffect register(String id, StatusEffect entry) {
        ENTRIES.put(new Identifier("ecosystemic", id), entry);
        return entry;
    }

    static void init() {
        ENTRIES.forEach((id, entry) -> Registry.register(Registry.STATUS_EFFECT, id, entry));
    }
}
