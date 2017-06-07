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

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.isEmpty
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion
import java.util.Objects


/**
 *
 * @author SirWellington
 */

val isNull = Matcher(Objects::isNull)
val notNull = !isNull
val notEmpty = !isEmpty


fun ThrowableAssertion.invalidArg(): ThrowableAssertion
{
    return this.isInstanceOf(tech.aroma.thrift.exceptions.InvalidArgumentException::class.java)
}

fun ThrowableAssertion.illegalArg(): ThrowableAssertion
{
    return this.isInstanceOf(IllegalArgumentException::class.java)
}

fun ThrowableAssertion.operationError(): ThrowableAssertion
{
    return this.isInstanceOf(tech.aroma.thrift.exceptions.OperationFailedException::class.java)
}

fun ThrowableAssertion.doesNotExist(): ThrowableAssertion
{
    return this.isInstanceOf(tech.aroma.thrift.exceptions.DoesNotExistException::class.java)
}

fun ThrowableAssertion.failedAssertion(): ThrowableAssertion
{
    return this.isInstanceOf(tech.sirwellington.alchemy.arguments.FailedAssertionException::class.java)
}