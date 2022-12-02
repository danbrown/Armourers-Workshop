package moe.plushie.armourers_workshop.compatibility.v19;

import com.google.gson.JsonObject;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IArgumentSerializer;
import moe.plushie.armourers_workshop.api.common.IArgumentType;
import moe.plushie.armourers_workshop.init.provider.CommonNativeFactory;
import moe.plushie.armourers_workshop.init.provider.CommonNativeProvider;
import moe.plushie.armourers_workshop.utils.TranslateUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Consumer;

@Available("[1.19, )")
public interface CommonNativeProviderExt_V1920 extends CommonNativeProvider, CommonNativeFactory {

    void willRegisterArgumentInfo(Consumer<ArgumentInfoRegistry> consumer);

    @Override
    default void willRegisterArgument(Consumer<ArgumentRegistry> consumer) {
        willRegisterArgumentInfo(registry1 -> consumer.accept(new ArgumentRegistry() {
            @Override
            public <T extends IArgumentType<?>> void register(ResourceLocation registryName, Class<T> argumentType, IArgumentSerializer<T> argumentSerializer) {
                registry1.register(registryName, argumentType, new ArgumentTypeInfo1920<>(argumentSerializer));
            }
        }));
    }

    @Override
    default MutableComponent createTranslatableComponent(String key, Object... args) {
        return MutableComponent.create(new TranslatableContents(key, args) {
            @Override
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> consumer, Style style) {
                String[] lastStyle = {""};
                return super.visit((style1, value) -> {
                    String embeddedStyle = lastStyle[0];
                    lastStyle[0] = embeddedStyle + TranslateUtils.getEmbeddedStyle(value);
                    return consumer.accept(style1, embeddedStyle + TranslateUtils.getFormattedString(value));
                }, style);
            }
        });
    }

    class ArgumentTypeInfo1920<A extends IArgumentType<?>> implements ArgumentTypeInfo<A, ArgumentTypeInfo1920.Template<A>> {

        private final IArgumentSerializer<A> serializer;

        public ArgumentTypeInfo1920(IArgumentSerializer<A> serializer) {
            this.serializer = serializer;
        }

        @Override
        public void serializeToNetwork(Template<A> template, FriendlyByteBuf friendlyByteBuf) {
            template.serializer.serializeToNetwork(template.instance, friendlyByteBuf);
        }

        @Override
        public Template<A> deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return unpack(serializer.deserializeFromNetwork(friendlyByteBuf));
        }

        @Override
        public void serializeToJson(Template<A> template, JsonObject jsonObject) {
            template.serializer.serializeToJson(template.instance, jsonObject);
        }

        @Override
        public Template<A> unpack(A argumentType) {
            return new Template<>(argumentType, serializer);
        }

        public static class Template<A extends IArgumentType<?>> implements ArgumentTypeInfo.Template<A> {

            private final A instance;
            private final IArgumentSerializer<A> serializer;

            public Template(A instance, IArgumentSerializer<A> serializer) {
                this.instance = instance;
                this.serializer = serializer;
            }

            @Override
            public A instantiate(CommandBuildContext commandBuildContext) {
                return instance;
            }

            @Override
            public ArgumentTypeInfo<A, ?> type() {
                return new ArgumentTypeInfo1920<>(serializer);
            }
        }
    }

    interface ArgumentInfoRegistry {

        <T extends IArgumentType<?>> void register(ResourceLocation registryName, Class<T> argumentType, ArgumentTypeInfo<T, ?> argumentInfo);
    }
}
