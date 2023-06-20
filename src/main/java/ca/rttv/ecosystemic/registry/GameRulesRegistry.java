package ca.rttv.ecosystemic.registry;

import net.minecraft.world.GameRules;

public class GameRulesRegistry {
    public static final GameRules.Key<GameRules.IntRule> ECOSYSTEMIC_VISITABLE_SPACES_CALCULATE_INTERVAL = register("ecosystemicVisitableSpacesCalculateInterval", GameRules.Category.MOBS, GameRules.IntRule.create(200));

    public static void init() {}

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Category category, GameRules.Type<T> type) {
        GameRules.Key<T> key = new GameRules.Key<>(name, category);
        GameRules.RULE_TYPES.put(key, type);
        return key;
    }
}
