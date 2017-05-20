package tech.aroma.data.sql.serializers

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

import com.nhaarman.mockito_kotlin.*
import org.junit.runner.RunWith
import tech.sirwellington.alchemy.test.junit.runners.*

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.jdbc.UncategorizedSQLException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.sql.*
import tech.aroma.data.sql.serializers.Tables.Users
import tech.aroma.thrift.User
import tech.aroma.thrift.generators.UserGenerators.users
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
@Repeat
class UserSerializerTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var row: ResultSet

    @GenerateString
    private lateinit var sql: String

    private lateinit var user: User
    private lateinit var userId: String

    @GenerateString(ALPHABETIC)
    private lateinit var invalidId: String

    private lateinit var instance: UserSerializer

    @Before
    fun setup()
    {
        instance = UserSerializer()

        user = one(users())
        userId = user.userId
        row.setupWith(user)
    }

    @Test
    fun testSave()
    {
        instance.save(user, sql, database)

        verify(database).update(sql,
                                user.userId.toUUID(),
                                user.firstName,
                                user.middleName,
                                user.lastName,
                                user.name,
                                user.email,
                                user.roles.toCommaSeparatedList(),
                                if (user.isSetBirthdate) user.birthdate.toTimestamp() else null,
                                user.profileImageLink.toUUID(),
                                user.githubProfile)
    }

    @DontRepeat
    @Test
    fun testSaveWithBadArgs()
    {
        assertThrows {
            instance.save(user, "", database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val emptyUser = User()
            instance.save(emptyUser, sql, database)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThrows {
            val invalidUser = User(user).setUserId(invalidId)
            instance.save(invalidUser, sql, database)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @DontRepeat
    @Test
    fun testSaveWhenDatabaseFails()
    {
        whenever(database.update(eq(sql), Mockito.anyVararg<Any>()))
                .thenThrow(UncategorizedSQLException::class.java)

        assertThrows { instance.save(user, sql, database) }
                .isInstanceOf(UncategorizedSQLException::class.java)
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(row)

        if (!user.isSetBirthdate) result.unsetBirthdate()

        assertEquals(user, result)
    }

    private fun ResultSet.setupWith(user: User)
    {
        whenever(this.getString(Users.USER_ID)).thenReturn(user.userId)
        whenever(this.getString(Users.FIRST_NAME)).thenReturn(user.firstName)
        whenever(this.getString(Users.MIDDLE_NAME)).thenReturn(user.middleName)
        whenever(this.getString(Users.LAST_NAME)).thenReturn(user.lastName)
        whenever(this.getString(Users.FULL_NAME)).thenReturn(user.name)
        whenever(this.getString(Users.PROFILE_IMAGE_ID)).thenReturn(user.profileImageLink)
        whenever(this.getString(Users.GITHUB_PROFILE)).thenReturn(user.githubProfile)
        whenever(this.getString(Users.EMAIL)).thenReturn(user.email)

        whenever(this.getDate(Users.BIRTH_DATE)).thenReturn(user.birthdate.toDate())

        val rolesArray = mock<java.sql.Array>
        {
            val backingArray = user.roles.toTypedArray()

            on { array }.thenReturn(backingArray)
        }

        whenever(this.getArray(Users.ROLES)).thenReturn(rolesArray)
    }
}