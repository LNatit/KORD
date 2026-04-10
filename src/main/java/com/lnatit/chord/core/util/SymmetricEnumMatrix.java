package com.lnatit.chord.core.util;

/**
 * 通用的二维对称枚举矩阵工具类
 * @param <E> 矩阵坐标轴的 Enum 类型
 * @param <V> 矩阵存储的值类型
 */
public class SymmetricEnumMatrix<E extends Enum<E>, V> {
    private final Object[][] grid;
    private final V defaultValue;

    /**
     * @param enumClass    枚举的 Class 对象，用于初始化数组大小
     * @param defaultValue 当矩阵中未显式定义时的默认返回值
     */
    public SymmetricEnumMatrix(Class<E> enumClass, V defaultValue) {
        int size = enumClass.getEnumConstants().length;
        this.grid = new Object[size][size];
        this.defaultValue = defaultValue;
    }

    /**
     * 核心：一次设置，双向生效，强制保证矩阵对称性
     */
    public void put(E e1, E e2, V value) {
        int idx1 = e1.ordinal();
        int idx2 = e2.ordinal();
        this.grid[idx1][idx2] = value;
        this.grid[idx2][idx1] = value;
    }

    /**
     * 批量设置相同行/列的快捷方法 (比如：任何动作遇上 GUI 都是 WARNING)
     */
    @SafeVarargs
    public final void putAll(E e1, V value, E... others) {
        for (E e2 : others) {
            put(e1, e2, value);
        }
    }

    /**
     * $O(1)$ 极速查表
     */
    @SuppressWarnings("unchecked")
    public V get(E e1, E e2) {
        Object val = this.grid[e1.ordinal()][e2.ordinal()];
        return val != null ? (V) val : defaultValue;
    }
}
