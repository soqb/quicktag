package io.github.soqb.quicktag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameRules;

public class QuickTag implements ModInitializer {
    public static final String MOD_ID = "quicktag";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final GameRules.Key<GameRules.BooleanRule> ENABLED =
            GameRuleRegistry.register("useQuickTagCommandStrategy", GameRules.Category.MISC,
                    GameRuleFactory.createBooleanRule(true));

    @Override
    public void onInitialize() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            QuickTagPersist.createFromServer(server, world);
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            TagRegistry<Entity> tags = TagRegistry.forServer(world.getServer());
            for (String name : entity.getCommandTags())
                // notably, while this does change values in the registry,
                // it merely shuffles entities between arrays, so no change to disk is needed.
                tags.tagPromiseNotToModify(name).load(entity);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            TagRegistry<Entity> tags = TagRegistry.forServer(world.getServer());
            for (String name : entity.getCommandTags())
                tags.tagPromiseNotToModify(name).unload(entity);
        });
    }
}
