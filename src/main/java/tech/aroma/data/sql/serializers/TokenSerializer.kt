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

package tech.aroma.data.sql.serializers

import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet
import java.time.Duration


/**
 *
 * @author SirWellington
 */
class TokenSerializer: DatabaseSerializer<AuthenticationToken>
{
    override fun save(`object`: AuthenticationToken?, timeToLive: Duration?, statement: String?, database: JdbcOperations?)
    {
        checkThat(`object`, database).are(notNull())
        checkThat(statement).`is`(nonEmptyString())

        val token = `object`!!
        val statement = statement!!
        val database = database!!


    }

    override fun deserialize(resultSet: ResultSet?): AuthenticationToken
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}