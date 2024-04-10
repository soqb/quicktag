package io.github.soqb.quicktag.mixin;

import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.common.base.Stopwatch;
import io.github.soqb.quicktag.QuickTag;
import io.github.soqb.quicktag.TagRegistry;
import io.github.soqb.quicktag.mixinhelper.HasCommandTags;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin implements HasCommandTags {
    private List<String> commandTags = new ArrayList<>();
    private Stopwatch queryStopwatch;

    @Shadow
    @Final
    private Function<Vec3d, Vec3d> positionOffset;

    @Shadow
    public abstract Predicate<Entity> getPositionPredicate(Vec3d pos);

    @Shadow
    public abstract <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities);

    @Override
    public List<String> getCommandTags() {
        return commandTags;
    }

    @Inject(method = "getUnfilteredEntities", at = @At("HEAD"))
    private void onHeadGetUnfilteredEntities(ServerCommandSource source,
            CallbackInfoReturnable<List<? extends Entity>> ci) {
        queryStopwatch = Stopwatch.createStarted();
    }

    private void finishTimer(ServerCommandSource source) {
        String msg =
                "entity selector query took " + queryStopwatch.elapsed().toString().toLowerCase();
        QuickTag.LOGGER.info(msg);
        if (source.getPlayer() != null)
            source.getPlayer().sendMessage(Text.literal(msg));
        queryStopwatch.stop();
    }

    @Inject(method = "getUnfilteredEntities", at = @At("RETURN"))
    private void onReturnGetUnfilteredEntities(ServerCommandSource source,
            CallbackInfoReturnable<List<? extends Entity>> ci) {
        finishTimer(source);
    }


    @Inject(method = "getUnfilteredEntities",
            at = @At(value = "INVOKE",
                    target = "com/google/common/collect/Lists.newArrayList()Ljava/util/ArrayList;",
                    ordinal = 0, remap = false),
            cancellable = true)
    private void onGetUnfiliteredEntities(ServerCommandSource source,
            CallbackInfoReturnable<List<? extends Entity>> ci) {
        if (commandTags.isEmpty() || !source.getWorld().getGameRules().getBoolean(QuickTag.ENABLED))
            return;

        TagRegistry<Entity> tags = TagRegistry.forServer(source.getServer());

        String smallestTagName = commandTags.get(0);
        int smallestTagSize = tags.tagPromiseNotToModify(smallestTagName).size();
        for (int i = 1; i < commandTags.size(); i++) {
            String tagName = commandTags.get(i);
            if (tags.tagPromiseNotToModify(tagName).size() < smallestTagSize) {
                smallestTagName = tagName;
            }
        }

        Vec3d pos = this.positionOffset.apply(source.getPosition());
        Predicate<Entity> predicate = this.getPositionPredicate(pos);
        List<Entity> entities = tags.tagPromiseNotToModify(smallestTagName).stream()
                .filter(predicate).collect(Collectors.toList());
        ci.setReturnValue(getEntities(pos, entities));
        finishTimer(source);
    }
}
