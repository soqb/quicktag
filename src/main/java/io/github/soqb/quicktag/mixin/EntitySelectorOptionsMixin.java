package io.github.soqb.quicktag.mixin;

import java.util.List;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.soqb.quicktag.mixinhelper.HasCommandTags;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorOptions.SelectorHandler;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {
    private static String currentId;

    @Inject(method = "putOption", at = @At(value = "HEAD"))
    private static void onPutOption(String id, SelectorHandler handler,
            Predicate<EntitySelectorReader> condition, Text description, CallbackInfo ci) {
        currentId = id;
    }

    @ModifyVariable(method = "putOption", argsOnly = true, at = @At(value = "HEAD"), index = 1)
    private static SelectorHandler modOptionHandler(SelectorHandler handler) {
        if (!"tag".equals(currentId))
            return handler;

        return (reader) -> {
            boolean bl = reader.readNegationCharacter();
            String string = reader.getReader().readUnquotedString();
            if ("".equals(string)) {
                reader.setPredicate(entity -> entity.getCommandTags().isEmpty() != bl);
            } else {
                reader.setPredicate(entity -> entity.getCommandTags().contains(string) != bl);

                List<String> tags = ((HasCommandTags) (Object) reader).getCommandTags();
                tags.add(string);
            }
        };
    }
}
