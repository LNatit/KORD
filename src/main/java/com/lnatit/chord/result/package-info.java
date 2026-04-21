/**
 * Result-layer model for conflict evaluation.
 *
 * <p>Current design assumptions:</p>
 * <ul>
 *     <li>{@link com.lnatit.chord.result.ContextPair} is directional. (A, B) and (B, A)
 *     are treated as different entries and are not normalized.</li>
 *     <li>{@link com.lnatit.chord.result.ConflictCollector.Context} owns mutable stage-local
 *     state ({@code finished} and dynamic risk lookup) for one directed context pair.</li>
 *     <li>{@link com.lnatit.chord.result.ConflictCollector.Meta} stores meta risks and
 *     context risks as collected; there is no cross-pair dedup or merge policy by design.</li>
 *     <li>{@link com.lnatit.chord.result.ConflictResult} is the final immutable snapshot with
 *     global severity, meta risks, and context-scoped risk lists.</li>
 *     <li>Evaluator stage ordering is defined in {@link com.lnatit.chord.eval.Evaluator}
 *     and is considered stable in the current architecture.</li>
 * </ul>
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
package com.lnatit.chord.result;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;