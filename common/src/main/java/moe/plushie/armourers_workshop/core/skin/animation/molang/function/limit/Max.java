package moe.plushie.armourers_workshop.core.skin.animation.molang.function.limit;

import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Function;

import java.util.List;

/**
 * {@link Function} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the greater of the two input values
 */
public final class Max extends Function {

    private final Expression valueA;
    private final Expression valueB;

    public Max(String name, List<Expression> arguments) {
        super(name, 2, arguments);
        this.valueA = arguments.get(0);
        this.valueB = arguments.get(1);
    }

    @Override
    public double getAsDouble() {
        return Math.max(valueA.getAsDouble(), valueB.getAsDouble());
    }
}
