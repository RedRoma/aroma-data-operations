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
import tech.aroma.data.assertions.RequestAssertions.validApplication
import tech.aroma.data.sql.*
import tech.aroma.thrift.Application
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import java.sql.ResultSet
import java.time.Duration


/**
 *
 * @author SirWellington
 */
internal class ApplicationSerializer : DatabaseSerializer<Application>
{
    override fun save(app: Application, timeToLive: Duration?, statement: String, database: JdbcOperations)
    {
        checkThat(app).`is`(validApplication())

        val appId = app.applicationId.asUUID()


        database.update(statement,
                        appId,
                        app.name,
                        app.applicationDescription,
                        app.organizationId.asUUID(),
                        app.programmingLanguage,
                        app.tier.toString(),
                        app.timeOfTokenExpiration.toTimestamp(),
                        app.applicationIconMediaId.asUUID())
    }

    override fun deserialize(resultSet: ResultSet): Application
    {
        val app = Application()


        return app
    }

}