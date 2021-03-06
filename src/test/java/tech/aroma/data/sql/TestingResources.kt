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


package tech.aroma.data.sql

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementSetter
import org.springframework.jdbc.core.RowMapper
import tech.aroma.data.sql.serializers.ModuleSerializers
import tech.aroma.thrift.Message

/**
 * @author SirWellington
 */

object TestingResources
{

    var LOG = LoggerFactory.getLogger(TestingResources::class.java)

    private val injector = Guice.createInjector(ModuleTesting(),
                                                ModuleSerializers())

    fun connectToDatabase(): JdbcOperations
    {
        return injector.getInstance(JdbcTemplate::class.java)
    }

    val messageSerializer: DatabaseSerializer<Message>
        get()
        {
            val literal = object : TypeLiteral<DatabaseSerializer<Message>>()
            {

            }
            return injector.getInstance(Key.get(literal))
        }
}

internal fun JdbcOperations.setupForFailure()
{
    whenever(this.update(any<String>()))
            .thenThrow(RuntimeException())

    whenever(this.update(any<String>(), Mockito.anyVararg<Any>()))
            .thenThrow(RuntimeException())

    whenever(this.update(any<String>(), any<PreparedStatementSetter>()))
            .thenThrow(RuntimeException())

    whenever(this.query(any<String>(), any<RowMapper<*>>()))
            .thenThrow(RuntimeException())

    whenever(this.query(any<String>(), any<RowMapper<*>>(), Mockito.anyVararg<Any>()))
            .thenThrow(RuntimeException())

    whenever(this.queryForObject(any<String>(), any<Class<*>>(), Mockito.anyVararg<Any>()))
            .thenThrow(RuntimeException())

    whenever(this.queryForObject(any<String>(), any<RowMapper<*>>(), Mockito.anyVararg<Any>()))
            .thenThrow(RuntimeException())

}