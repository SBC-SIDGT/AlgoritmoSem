/*
 * Copyright © 2018 - present | ThreadingTools by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 18/11/2018 - ThreadingTools.
 */

package com.github.javinator9889.utils;

import com.github.javinator9889.utils.errors.InvalidClassTypeException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

/**
 * <p>
 * {@code ArgumentParser} provides a <b>fully compatible</b> class with <i>plenty of objects</i>. It
 * is designed for using it as an <b>access platform</b> to methods' arguments and params, taking
 * advantage of {@code lambda expressions} of Java 8 and above.
 * <p>
 * It is not <b>thread safe</b> as all the operations are not done <i>atomically</i>, so there is no
 * guarantee that all the data stored at {@link HashMap} is saved in the order expected and with the
 * expected values if appending from multiple threads at the same time.
 */
public class ArgumentParser implements Serializable, Cloneable {
    /**
     * Default capacity of the {@linkplain #mArguments HashMap} object containing values - based on
     * an analysis of typical consumption of applications.
     */
    public static final int DEFAULT_CAPACITY = 8;

    /**
     * Default load factor for {@linkplain #mArguments HashMap} - when there is no capacity
     * specified, a better performance is obtained by setting it up to 0.75 as it is a good
     * approximation to {@code log(2)}, so its capacity increases when the 75% of the {@code
     * HashTable} is filled.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75F;

    /**
     * Optimum load factor used when an {@code initial capacity} is provided - if we know how much
     * objects (keys) are going to be stored inside the hash table, there is no need to increase its
     * capacity when, following the latest explanation, the 75% of the {@code HashTable} is filled,
     * as we will consume resources for increasing a table that will not be completely filled (if we
     * are going to store 16 objects, there is no need to increase the table capacity when there are
     * 12 objects stored for possibly saving 20 ones when we are going to only store 16 ones).
     */
    public static final float OPTIMUM_LOAD_FACTOR_BASED_ON_SIZE = 1.0F;

    /**
     * {@link HashMap} containing the params specified by the user that is working with this class.
     */
    private HashMap<String, Object> mArguments;

    /**
     * Default constructor, using the {@link #DEFAULT_CAPACITY default capacity (=8)} and the {@link
     * #DEFAULT_LOAD_FACTOR default load factor(=0.75)} for generating the {@link HashMap}.
     * <p>
     * It has the same behaviour as calling {@link #ArgumentParser(int, float)} with {@code
     * ArgumentParser(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR)} or {@code ArgumentParser(8, 0.75F)}.
     */
    public ArgumentParser() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Generates a new instance of {@link ArgumentParser this} by using the provided {@code
     * initialCapacity}. When calling this constructor, you must ensure that the {@code
     * initialCapacity} you provided <b>is as exact as possible</b>, for avoiding an <b>abusive
     * use</b> of <i>resources</i> (because when saving one more object than the specified one, the
     * hole table must be rewritten completely and duplicated, which consumes lots of resources and
     * it is not as fast as expected on {@link HashMap}). If you do not know exactly the initial
     * capacity, is better to use {@link #ArgumentParser()} with no arguments.
     * <p>
     * It has the same behaviour as calling {@link #ArgumentParser(int, float)} with {@code
     * ArgumentParser(initialCapacity, OPTIMUM_LOAD_FACTOR_BASED_ON_SIZE)} or {@code
     * ArgumentParser(initialCapacity, 1.0F)}.
     *
     * @param initialCapacity the amount of params that will be stored. It must be as exact as
     *                        possible.
     */
    public ArgumentParser(int initialCapacity) {
        this(initialCapacity, OPTIMUM_LOAD_FACTOR_BASED_ON_SIZE);
    }

    /**
     * Duplicates an existing {@link ArgumentParser} copying the existing params at the {@code from}
     * object.
     *
     * @param from {@link ArgumentParser} from which values will be copied.
     */
    public ArgumentParser(@NotNull ArgumentParser from) {
        mArguments = new HashMap<>(from.mArguments);
    }

    /**
     * Private constructor designed for generating optimized {@link HashMap} based on user
     * requirements. Its visibility is {@code private} because some users do not know which values
     * must be at {@code loadFactor} so the performance can be dramatically decreased.
     *
     * @param initialCapacity the amount of params that will be stored. It must be as exact as
     *                        possible, or using the {@link #DEFAULT_CAPACITY default capacity (=8)}
     *                        instead.
     * @param loadFactor      factor that will determine whether the hash table must be resized. It
     *                        must be {@code 1.0F} when the initial capacity is exact, else {@link
     *                        #DEFAULT_LOAD_FACTOR the default load factor (=0.75)}.
     */
    private ArgumentParser(int initialCapacity, float loadFactor) {
        mArguments = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)} should return {@code true} if and only if {@code
     * y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if {@code x.equals(y)} returns {@code true} and {@code
     * y.equals(z)} returns {@code true}, then {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of {@code x.equals(y)} consistently return
     * {@code true} or consistently return {@code false}, provided no information used in {@code
     * equals} comparisons on the objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements the most discriminating
     * possible equivalence relation on objects; that is, for any non-null reference values {@code
     * x} and {@code y}, this method returns {@code true} if and only if {@code x} and {@code y}
     * refer to the same object ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode} method whenever this
     * method is overridden, so as to maintain the general contract for the {@code hashCode} method,
     * which states that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     *
     * @see #hashCode()
     * @see java.util.HashMap
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArgumentParser that = (ArgumentParser) obj;
        return Objects.equals(mArguments, that.mArguments);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash
     * tables such as those provided by {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the {@code hashCode} method must consistently return the
     * same integer, provided no information used in {@code equals} comparisons on the object is
     * modified. This integer need not remain consistent from one execution of an application to
     * another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     * method, then calling the {@code hashCode} method on each of the two objects must produce the
     * same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link java.lang.Object#equals(java.lang.Object)} method, then calling the
     * {@code hashCode} method on each of the two objects must produce distinct integer results.
     * However, the programmer should be aware that producing distinct integer results for unequal
     * objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by class {@code Object} does
     * return distinct integers for distinct objects. (The hashCode may or may not be implemented as
     * some function of an object's memory address at some point in time.)
     *
     * @return a hash code value for this object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(mArguments);
    }

    /**
     * Returns a string containing a concise, human-readable description of this object.
     *
     * @return a printable representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String classDefinition = super.toString();
        builder.append(classDefinition).append("\n").append('[');
        boolean loopFirstExecution = true;
        for (String paramName : mArguments.keySet()) {
            String paramValue = getAsString(paramName);
            if (!loopFirstExecution)
                builder.append(", ");
            else
                loopFirstExecution = false;
            builder.append(paramName).append("=").append(paramValue);
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Adds all values from other {@linkplain ArgumentParser} to the current {@code arguments}.
     *
     * @param params the {@code ArgumentParser} from which copying values.
     */
    public void putAllParams(@NotNull ArgumentParser params) {
        mArguments.putAll(params.mArguments);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull String paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Byte paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Short paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Integer paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Long paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Float paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Double paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Boolean paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull byte[] paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull Object paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName  name of the param to store.
     * @param paramValue the data of the param.
     */
    public void putParam(@NotNull String paramName, @NotNull List<?> paramValue) {
        mArguments.put(paramName, paramValue);
    }

    /**
     * Adds a value to the param set.
     *
     * @param paramName name of the param to store.
     */
    public void putNullParam(@NotNull String paramName) {
        mArguments.put(paramName, null);
    }

    /**
     * Returns the number of params.
     *
     * @return the number of parameters.
     */
    public int size() {
        return mArguments.size();
    }

    /**
     * Indicates whether this collection is empty.
     *
     * @return true if size == 0
     */
    public boolean isEmpty() {
        return mArguments.isEmpty();
    }

    /**
     * Remove a single value.
     *
     * @param paramName the name of the value to remove
     */
    public void remove(@NotNull String paramName) {
        mArguments.remove(paramName);
    }

    /**
     * Removes all params.
     */
    public void clear() {
        mArguments.clear();
    }

    /**
     * Returns true if this object has the named value.
     *
     * @param paramName the value to check for
     *
     * @return {@code true} if the value is present, {@code false} otherwise
     */
    public boolean containsParam(@NotNull String paramName) {
        return mArguments.containsKey(paramName);
    }

    /**
     * Gets a value. Valid value types are {@link String}, {@link Boolean}, {@link Number}, {@code
     * byte[]} and {@link Object} implementations.
     *
     * @param paramName the value to get.
     *
     * @return the data for the value, or {@code null} if the value is missing or if {@code null}
     * was previously added with the given {@code key}.
     */
    public Object get(@NotNull String paramName) {
        return mArguments.get(paramName);
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@link String} if present, {@code null} if not.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link String}.
     */
    public String getString(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof String || paramValue == null)
            return (String) paramValue;
        else
            throw new InvalidClassTypeException("Invalid class: param stored is not String, but " +
                    paramValue.getClass().getSimpleName());
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code int} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Integer} or it is {@code null}.
     */
    public int getInt(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Integer)
            return ((Number) paramValue).intValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Int value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Integer.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Integer value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Integer, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code byte} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Byte} or it is {@code null}.
     */
    public byte getByte(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Byte)
            return ((Number) paramValue).byteValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Byte value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Byte.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Byte value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Byte, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code short} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Short} or it is {@code null}.
     */
    public short getShort(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Short)
            return ((Number) paramValue).shortValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Short value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Short.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Short value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Short, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code long} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Long} or it is {@code null}.
     */
    public long getLong(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Long)
            return ((Number) paramValue).longValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Long value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Long.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Long value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Long, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code float} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Float} or it is {@code null}.
     */
    public float getFloat(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Float)
            return ((Number) paramValue).floatValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Float value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Float.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Float value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Float, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code double} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Double} or it is {@code null}.
     */
    public double getDouble(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Double)
            return ((Number) paramValue).doubleValue();
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Double value cannot be null");
            if (paramValue instanceof CharSequence)
                try {
                    return Double.valueOf(paramValue.toString());
                } catch (NumberFormatException ignored) {
                    throw new InvalidClassTypeException("Cannot parse Double value for value \""
                            + paramValue + "\" at param: " + paramName);
                }
            throw new InvalidClassTypeException("Invalid class: param stored is not Double, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code boolean} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Boolean} or it is {@code null}.
     */
    public boolean getBoolean(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof Boolean)
            return (Boolean) paramValue;
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Boolean value cannot be null");
            if (paramValue instanceof CharSequence)
                return Boolean.valueOf(paramValue.toString()) || "1".equals(paramValue);
            else if (paramValue instanceof Number)
                return ((Number) paramValue).intValue() != 0;
            else
                throw new InvalidClassTypeException("Cannot parse Boolean value for value \""
                        + paramValue + "\" at param: " + paramName);
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code byte} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link Byte} or it is {@code null}.
     */
    public byte[] getBytes(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof byte[])
            return (byte[]) paramValue;
        else {
            if (paramValue == null)
                throw new InvalidClassTypeException("Byte[] value cannot be null");
            throw new InvalidClassTypeException("Invalid class: param stored is not Byte[], but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the param value stored under the given param name.
     *
     * @param paramName param name from which obtaining the value.
     *
     * @return {@code List} if present.
     *
     * @throws InvalidClassTypeException whether the object contained at {@code paramName} is not an
     *                                   {@link List} or it is {@code null}.
     */
    public List<?> getList(@NotNull String paramName) {
        Object paramValue = mArguments.getOrDefault(paramName, null);
        if (paramValue instanceof List<?>) {
            return (List<?>) paramValue;
        } else {
            if (paramValue == null)
                throw new InvalidClassTypeException("List<?> value cannot be null");
            throw new InvalidClassTypeException("Invalid class: param stored is not List<?>, but " +
                    paramValue.getClass().getSimpleName());
        }
    }

    /**
     * Obtains the {@link String} representation of the value contained at {@code paramName}.
     *
     * @param paramName name of the param from which value is obtained.
     *
     * @return {@link String} representation of the value.
     */
    public String getAsString(@NotNull String paramName) {
        Object value = mArguments.get(paramName);
        return value != null ? value.toString() : null;
    }

    /**
     * Returns a set of all of the params names' and params values'.
     *
     * @return a set of all of the params names' and params values'.
     */
    public Set<Map.Entry<String, Object>> paramsValuesSet() {
        return mArguments.entrySet();
    }

    /**
     * Returns a set of all params names'.
     *
     * @return a set of all params names'.
     */
    public Set<String> paramsNamesSet() {
        return mArguments.keySet();
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning of "copy" may depend on the
     * class of the object. The general intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements. While it is typically the case
     * that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling {@code super.clone}.  If a
     * class and all of its superclasses (except {@code Object}) obey this convention, it will be
     * the case that {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent of this object (which
     * is being cloned).  To achieve this independence, it may be necessary to modify one or more
     * fields of the object returned by {@code super.clone} before returning it.  Typically, this
     * means copying any mutable objects that comprise the internal "deep structure" of the object
     * being cloned and replacing the references to these objects with references to the copies.  If
     * a class contains only primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone} need to be modified.
     * <p>
     * The method {@code clone} for class {@code Object} performs a specific cloning operation.
     * First, if the class of this object does not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays are considered to
     * implement the interface {@code Cloneable} and that the return type of the {@code clone}
     * method of an array type {@code T[]} is {@code T[]} where T is any reference or primitive
     * type. Otherwise, this method creates a new instance of the class of this object and
     * initializes all its fields with exactly the contents of the corresponding fields of this
     * object, as if by assignment; the contents of the fields are not themselves cloned. Thus, this
     * method performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface {@code Cloneable}, so
     * calling the {@code clone} method on an object whose class is {@code Object} will result in
     * throwing an exception at run time.
     *
     * @return a clone of this instance.
     *
     * @throws IllegalStateException if there was an error calling {@linkplain Object#clone()} so it
     *                               throws an {@linkplain CloneNotSupportedException}.
     * @see Cloneable
     */
    @Override
    @SuppressWarnings("unchecked")
    public final ArgumentParser clone() {
        final ArgumentParser clone;
        try {
            clone = (ArgumentParser) super.clone();
        } catch (CloneNotSupportedException notPossibleException) {
            throw new IllegalStateException("Clone not supported(¿?)", notPossibleException);
        }
        clone.mArguments = (HashMap<String, Object>) this.mArguments.clone();
        return clone;
    }
}
