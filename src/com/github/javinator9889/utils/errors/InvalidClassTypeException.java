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

package com.github.javinator9889.utils.errors;

/**
 * Custom exception called when retrieving data from {@link com.github.javinator9889.utils.ArgumentParser}
 * and the data type <b>is not the specified one</b>. For example, if you call {@link
 * com.github.javinator9889.utils.ArgumentParser#getInt(String)} and the value assigned to that
 * {@code String} is not an {@code Integer} but a {@code ArrayList}, this exception is thrown.
 * <p>
 * Also, in the same example as above, if the value is {@code null}, this exception is also thrown
 * as <b>{@code Integers} cannot be {@code null}</b>.
 */
public class InvalidClassTypeException extends RuntimeException {
    /**
     * Constructs a new runtime exception with the specified detail message. The cause is not
     * initialized, and may subsequently be initialized by a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public InvalidClassTypeException(String message) {
        super(message);
    }
}
