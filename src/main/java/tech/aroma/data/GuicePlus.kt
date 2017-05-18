/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.data

import com.google.inject.*
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder

/*
    Shortcut functions for handling Guice with Kotlin.
 */

/**
 *
 * @author SirWellington
 */
internal inline fun <reified T : Any> Binder.bind(): AnnotatedBindingBuilder<T>
{
    val literal = object : TypeLiteral<T>() {}

    return bind(literal)
}

internal inline fun <reified T : Any> AnnotatedBindingBuilder<in T>.to(): ScopedBindingBuilder = to(T::class.java)

internal inline fun <reified T : Any> Injector.getInstance() = getInstance(T::class.java)

internal inline fun <reified T> Injector.hasInstance(): Boolean
{
    val literal = object : TypeLiteral<T>() {}

    val result = this.getInstance(Key.get(literal))
    return result != null
}