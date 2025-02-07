package moe.plushie.armourers_workshop.core.skin.animation.molang.function.limit;

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
 * Returns the first input value if is larger than the second input value and less than the third input value; or else returns the nearest of the second two input values
 */
public final class Clamp extends Function {

    private final Expression value;
    private final Expression min;
    private final Expression max;

    public Clamp(String name, List<Expression> arguments) {
        super(name, 3, arguments);
        this.value = arguments.get(0);
        this.min = arguments.get(1);
        this.max = arguments.get(2);
    }

    @Override
    public double getAsDouble() {
        return MathHelper.clamp(value.getAsDouble(), min.getAsDouble(), max.getAsDouble());
    }
}
