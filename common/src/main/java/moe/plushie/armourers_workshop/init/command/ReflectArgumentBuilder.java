package moe.plushie.armourers_workshop.init.command;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import moe.plushie.armourers_workshop.core.utils.Objects;
import moe.plushie.armourers_workshop.init.network.ExecuteCommandPacket;
import moe.plushie.armourers_workshop.init.platform.NetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReflectArgumentBuilder<S> extends LiteralArgumentBuilder<S> {

    private static final ImmutableMap<Class<?>, Function<Pair<Object, Field>, ArgumentBuilder<CommandSourceStack, ?>>> FIELD_BUILDERS =
            new ImmutableMap.Builder<Class<?>, Function<Pair<Object, Field>, ArgumentBuilder<CommandSourceStack, ?>>>()
                    .put(boolean.class, pair -> argument(pair, BoolArgumentType.bool(), BoolArgumentType::getBool))
                    .put(int.class, pair -> argument(pair, IntegerArgumentType.integer(), IntegerArgumentType::getInteger))
                    .put(double.class, pair -> argument(pair, DoubleArgumentType.doubleArg(), DoubleArgumentType::getDouble))
                    .put(float.class, pair -> argument(pair, FloatArgumentType.floatArg(), FloatArgumentType::getFloat))
                    .put(String.class, pair -> argument(pair, StringArgumentType.string(), StringArgumentType::getString))
                    .build();

    private final Class<?> object;

    protected ReflectArgumentBuilder(String literal, Class<?> object) {
        super(literal);
        this.object = object;
    }

    public static ReflectArgumentBuilder<CommandSourceStack> literal(final String name, Class<?> object) {
        return new ReflectArgumentBuilder<>(name, object);
    }

    public static <R> ArgumentBuilder<CommandSourceStack, ?> argument(Pair<Object, Field> pair, ArgumentType<R> argumentType, BiFunction<CommandContext<?>, String, R> argumentParser) {
        return Commands.literal(pair.getValue().getName())
                .then(Commands.argument("value", argumentType).executes(context -> {
                    R value = argumentParser.apply(context, "value");
                    String name = pair.getValue().getName();
                    Class<?> object = (Class<?>) pair.getKey();
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    NetworkManager.sendTo(ExecuteCommandPacket.set(object, name, value), player);
                    return 0;
                }))
                .executes(context -> {
                    String name = pair.getValue().getName();
                    Class<?> object = (Class<?>) pair.getKey();
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    NetworkManager.sendTo(ExecuteCommandPacket.get(object, name), player);
                    return 0;
                });
    }

//    public static <R> ArgumentBuilder<CommandSourceStack, ?> call(Pair<Object, Method> pair) {
//        return Commands.literal(pair.getValue().getName() + "()")
//                .executes(context -> {
//                    String name = pair.getValue().getName();
//                    Class<?> object = (Class<?>) pair.getKey();
//                    ServerPlayer player = context.getSource().getPlayerOrException();
//                    NetworkManager.sendTo(ExecuteCommandPacket.invoke(object, name), player);
//                    return 0;
//                });
//    }

    @Override
    public Collection<CommandNode<S>> getArguments() {
        ArrayList<CommandNode<S>> nodes = new ArrayList<>(super.getArguments());
        for (Field field : object.getDeclaredFields()) {
            Function<Pair<Object, Field>, ArgumentBuilder<CommandSourceStack, ?>> function = FIELD_BUILDERS.get(field.getType());
            if (function != null) {
                nodes.add(Objects.unsafeCast(function.apply(Pair.of(object, field)).build()));
            }
        }
//        for (Method method : object.getMethods()) {
//            if (method.getParameterCount() == 0 && Modifier.isStatic(method.getModifiers())) {
//                nodes.add(Objects.unsafeCast(ReflectArgumentBuilder.call(Pair.of(object, method)).build()));
//            }
//        }
        return nodes;
    }
}
