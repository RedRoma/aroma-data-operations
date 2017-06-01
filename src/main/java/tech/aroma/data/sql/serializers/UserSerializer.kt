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
import tech.aroma.data.assertions.RequestAssertions.validUser
import tech.aroma.data.sql.*
import tech.aroma.data.sql.serializers.Columns.Users
import tech.aroma.thrift.Role
import tech.aroma.thrift.User
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet


/**
 * Responsible for serializing [Users][User]
 * @author SirWellington
 */
internal class UserSerializer : DatabaseSerializer<User>
{
    override fun save(user: User, statement: String, database: JdbcOperations)
    {
        checkThat(user).`is`(validUser())
        checkThat(statement).`is`(nonEmptyString())

        val birthday = if (user.isSetBirthdate) user.birthdate else null

        database.update(statement,
                        user.userId.toUUID(),
                        user.firstName,
                        user.middleName,
                        user.lastName,
                        user.name,
                        user.email,
                        user.roles?.toCommaSeparatedList(),
                        birthday?.toTimestamp(),
                        user.profileImageLink?.toUUID(),
                        user.githubProfile)
    }

    override fun deserialize(row: ResultSet): User
    {
        val userId = row.getString(Users.USER_ID)
        val firstName = row.getString(Users.FIRST_NAME)
        val middleName = row.getString(Users.MIDDLE_NAME)
        val lastName = row.getString(Users.LAST_NAME)
        val fullName = row.getString(Users.FULL_NAME)
        val email: String? = row.getString(Users.EMAIL)
        val roles = row.getArray(Users.ROLES)?.toRoles()
        val birthdate = row.getDate(Users.BIRTH_DATE)
        val profileImageId = row.getString(Users.PROFILE_IMAGE_ID)
        val githubProfile = row.getString(Users.GITHUB_PROFILE)
        val timeCreated = row.getTime(Users.TIME_ACCOUNT_CREATED) ?: null

        val user = User()
                .setUserId(userId)
                .setFirstName(firstName)
                .setMiddleName(middleName)
                .setLastName(lastName)
                .setName(fullName)
                .setEmail(email)
                .setGithubProfile(githubProfile)
                .setProfileImageLink(profileImageId)

        if (timeCreated != null)
        {
            user.timeUserJoined = timeCreated.time
        }

        if (roles != null)
        {
            user.setRoles(roles.toSet())
        }

        if (birthdate != null)
        {
            user.setBirthdate(birthdate.time)
        }

        return user
    }

    private fun java.sql.Array.toRoles(): List<Role>?
    {
        val array = this.array as? Array<*> ?: return null

        return array.map { it.toString() }
                    .map { it.toRole() }
                    .filterNotNull()
    }

    private fun String.toRole(): Role?
    {
        return try
        {
            Role.valueOf(this)
        }
        catch(ex: Exception)
        {
            return null
        }
    }
}