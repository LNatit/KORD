/**
 * TODO inspect needed!!!
 * Result-layer model for conflict evaluation.
 *
 * <p>Key design points after collector refactor:</p>
 * <ul>
 *     <li>{@link com.lnatit.chord.result.ContextPair} is unordered, so (A, B) and (B, A)
 *     are merged into the same key.</li>
 *     <li>{@link com.lnatit.chord.result.ConflictResult} stores pair-scoped risks in
 *     {@code Map<ContextPair, List<ConflictRisk>>}, plus a separate meta-risk list.</li>
 *     <li>{@link com.lnatit.chord.result.PairConflictCollector} is dedicated to a single
 *     semantic-context pair and owns mutable stage state such as {@code finished} and dynamic-risk lookup.</li>
 *     <li>{@link com.lnatit.chord.result.MetaConflictCollector} aggregates meta tags and pair results,
 *     and is the only collector that can emit final {@link com.lnatit.chord.result.ConflictResult}.</li>
 * </ul>
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
package com.lnatit.chord.result;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;