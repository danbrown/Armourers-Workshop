package moe.plushie.armourers_workshop.core.skin.animation.molang.function.round;

import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Function;
import moe.plushie.armourers_workshop.core.skin.animation.molang.impl.MathHelper;

import java.util.List;

/**
 * {@link Function} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the first value plus the difference between the first and second input values multiplied by the third input value, wrapping the end result as a degrees value
 */
public final class LerpRot extends Function {

    private final Expression min;
    private final Expression max;
    private final Expression delta;

    public LerpRot(String name, List<Expression> arguments) {
        super(name, 3, arguments);
        this.min = arguments.get(0);
        this.max = arguments.get(1);
        this.delta = arguments.get(2);
    }

    @Override
    public double getAsDouble() {
        return MathHelper.lerpYaw(delta.getAsDouble(), min.getAsDouble(), max.getAsDouble());
    }
}
