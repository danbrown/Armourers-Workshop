package moe.plushie.armourers_workshop.core.skin.animation.molang.core;

import moe.plushie.armourers_workshop.core.skin.animation.molang.impl.Supplier;
import moe.plushie.armourers_workshop.core.skin.animation.molang.impl.Visitor;

/**
 * The expression interface. It's the super-interface for
 * all the expression types.
 *
 * <p>Expressions are evaluable parts of code, expressions
 * are emitted by the parser.</p>
 *
 * <p>In Molang, almost every expression evaluates to a numerical
 * value</p>
 */
public interface Expression extends Supplier {

    /**
     * Visits this expression with the given visitor.
     *
     * @param visitor The expression visitor
     * @return The visit result
     */
    Expression visit(final Visitor visitor);

    /**
     * Return whether this type of MathValue should be considered mutable; its value could change.
     * <br>
     * This is used to cache calculated values, optimising computational work
     */
    default boolean isMutable() {
        return true;
    }
}
