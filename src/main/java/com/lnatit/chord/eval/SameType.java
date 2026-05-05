package com.lnatit.chord.eval;

import com.lnatit.chord.result.Scene;
import com.lnatit.chord.util.AsymmetricEnumMatrix;

/**
 * <p>动态分发层的关系压缩枚举。{@link Evaluator#evalDynamic(KeyPair)} 先分别计算“主键位置”与“修饰键位置”相对于另一绑定的关系，
 * 再通过 {@link #toScene(SameType, SameType)} 查询 {@link #PRESS_MATRIX} / {@link #RELEASE_MATRIX}，最后交给
 * {@link Scene} 合成为具体地分发场景。</p>
 *
 * <p>判定目标不是“两个绑定抽象上是否相似”，而是：对于任意一侧按键，是否存在一个输入态，使 NeoForge 会把这两个绑定同时放进候选列表。
 * 这里的依据对应 NeoForge 的
 * <a href="https://github.com/neoforged/NeoForge/blob/1.21.x/src/main/java/net/neoforged/neoforge/client/settings/KeyMappingLookup.java">KeyMappingLookup#getAll(InputConstants.Key, boolean)</a>
 * 逻辑。PRESS 相位存在“非 NONE 命中优先、NONE 仅在无命中时 fallback”的约束；RELEASE 相位则会额外处理 modifier release，
 * 且总会补一次 NONE fallback，因此两张矩阵必须分开维护。</p>
 *
 * <p>枚举项含义按“当前位置”定义，而不是按字段名定义：</p>
 * <ul>
 *     <li>{@link #MAIN}：当前位置绑定的值，等于另一绑定的主键。</li>
 *     <li>{@link #MODIFIER}：当前位置绑定的值，等于另一绑定的修饰键。</li>
 *     <li>{@link #DISJOINT}：当前位置绑定的值，与另一绑定既不等于其主键也不等于其修饰键。</li>
 * </ul>
 *
 * <p>矩阵坐标可记作两位记号 XY：</p>
 * <ul>
 *     <li>X 表示当前绑定主键相对另一绑定的关系。</li>
 *     <li>Y 表示当前绑定修饰键相对另一绑定的关系。</li>
 * </ul>
 *
 * <p>以下给出每种有效路径的结论与依据：</p>
 * <ul>
 *     <li><b>KK / MM：</b>不可能路径。对于单个绑定，自身主键与修饰键不可能相等，因此不进入矩阵讨论。</li>
 *     <li><b>DD：</b>PRESS=false，RELEASE=false。两侧既不共享主键，也不共享修饰键，不存在共同候选来源。</li>
 *     <li><b>KM：</b>PRESS=true，RELEASE=true。两绑定同主键同修饰键，属于完全同签名，按下与释放都会共同命中。</li>
 *     <li><b>MK：</b>PRESS=true，RELEASE=true。属于主键/修饰键交换路径；NeoForge 在“当前按下的是修饰键”时会额外遍历其他修饰键作为主键的绑定，因此 PRESS 可共同命中；RELEASE 也同样成立。</li>
 *     <li><b>KD：</b>PRESS=false，RELEASE=true。两绑定主键相同但修饰键不同时，PRESS 只要非 NONE 绑定先命中，就会抑制 NONE fallback，因此不会共同返回；RELEASE 则总会补 NONE fallback，所以可共同命中。</li>
 *     <li><b>DK：</b>PRESS=false，RELEASE=true。一侧主键等于另一侧修饰键时，PRESS 不足以让两边同时进入候选列表；RELEASE 时释放该修饰键会额外释放所有使用它作为 modifier 的绑定，因此为 true。</li>
 *     <li><b>DM：</b>PRESS=false，RELEASE=true。两绑定主键不同但修饰键相同，PRESS 没有共同主键入口；RELEASE 时释放这个共享 modifier 会同时触发两边。</li>
 *     <li><b>MD：</b>PRESS=false，RELEASE=true。与 DK 对称；PRESS 不成立，RELEASE 由于 modifier release 补集成立。</li>
 * </ul>
 *
 * <p>因此最终可归纳为：</p>
 * <ul>
 *     <li>PRESS：只有 KM、MK 为 true。</li>
 *     <li>RELEASE：除 DD 外，其余有效路径都为 true。</li>
 * </ul>
 *
 * <p>后续若矩阵值调整，应始终以 {@code KeyMappingLookup#getAll(...)} 的真实分发语义为准；本类只负责把该语义压缩成小型查表结构，
 * 供 {@link Evaluator#evalDynamic(KeyPair)} 快速使用。</p>
 */
public enum SameType
{
    MAIN, MODIFIER, DISJOINT;

    public static final AsymmetricEnumMatrix<SameType, Boolean> PRESS_MATRIX =
            new AsymmetricEnumMatrix<>(SameType.class, false);
    public static final AsymmetricEnumMatrix<SameType, Boolean> RELEASE_MATRIX =
            new AsymmetricEnumMatrix<>(SameType.class, false);

    static {
        // TODO init matrix, combine two matrices
    }

    public static Scene toScene(SameType key, SameType modifier) {
        boolean press = PRESS_MATRIX.get(key, modifier);
        boolean release = RELEASE_MATRIX.get(key, modifier);
        return new Scene(press, release);
    }
}
