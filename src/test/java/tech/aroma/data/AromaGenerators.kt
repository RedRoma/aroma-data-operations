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

import tech.aroma.thrift.authentication.AuthenticationToken
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.ObjectGenerators.pojos
import tech.sirwellington.alchemy.generator.StringGenerators.uuids
import tech.sirwellington.alchemy.generator.TimeGenerators


/**
 *
 * @author SirWellington
 */


class AromaGenerators
{

    companion object
    {

        val id: String get() = uuids.get()

        val pastTimes: Long
            get() = TimeGenerators.pastInstants().get().toEpochMilli()

        val futureTimes: Long
            get() = TimeGenerators.futureInstants().get().toEpochMilli()

        val currentTimes: Long
            get() = TimeGenerators.presentInstants().get().toEpochMilli()

        val token: AuthenticationToken
            get()
            {
                val token = one(pojos(AuthenticationToken::class.java))
                token.unsetOrganizationName()

                return token
                        .setTokenId(id)
                        .setOrganizationId(id)
                        .setOwnerId(id)
                        .setTimeOfCreation(currentTimes)
                        .setTimeOfExpiration(futureTimes)
            }
    }

}