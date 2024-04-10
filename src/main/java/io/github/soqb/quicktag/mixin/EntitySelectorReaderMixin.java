package io.github.soqb.quicktag.mixin;

import net.fabricmc.fabric.api.command.v2.FabricEntitySelectorReader;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.github.soqb.quicktag.mixinhelper.HasCommandTags;

@Mixin(EntitySelectorReader.class)
public abstract class EntitySelectorReaderMixin
        implements FabricEntitySelectorReader, HasCommandTags {
    private List<String> commandTags = new ArrayList<>();

    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void onBuild(CallbackInfoReturnable<EntitySelector> ci) {
        EntitySelector sel = ci.getReturnValue();
        ((HasCommandTags) (Object) sel).getCommandTags().addAll(commandTags);
        ci.setReturnValue(sel);
    }

    @Override
    public List<String> getCommandTags() {
        return commandTags;
    }
}
