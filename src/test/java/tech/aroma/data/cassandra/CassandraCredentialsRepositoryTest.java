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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.data.cassandra.Tables.Credentials;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.*;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraCredentialsRepositoryTest
{

    @Mock
    private Session cassandra;

    private CassandraCredentialsRepository instance;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @GenerateString(HEXADECIMAL)
    private String password;

    @Mock
    private ResultSet results;

    @Mock
    private Row row;

    @Captor
    private ArgumentCaptor<Statement> captor;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new CassandraCredentialsRepository(cassandra);
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {
        when(results.one()).thenReturn(row);
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenReturn(results);

    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraCredentialsRepository(null))
            .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testSaveEncryptedPassword() throws Exception
    {
        instance.saveEncryptedPassword(userId, password);

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.class)));
    }

    @DontRepeat
    @Test
    public void testSaveEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveEncryptedPassword(badId, password))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveEncryptedPassword("", password))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveEncryptedPassword(userId, ""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testContainsEncryptedPassword() throws Exception
    {
        when(row.getLong(0)).thenReturn(0L);
        assertThat(instance.containsEncryptedPassword(userId), is(false));

        when(row.getLong(0)).thenReturn(1L);
        assertThat(instance.containsEncryptedPassword(userId), is(true));
    }

    @DontRepeat
    @Test
    public void testContainsEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsEncryptedPassword(""))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.containsEncryptedPassword(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetEncryptedPassword() throws Exception
    {
        when(row.getString(Credentials.ENCRYPTED_PASSWORD))
            .thenReturn(password);

        String result = instance.getEncryptedPassword(userId);
        assertThat(result, is(password));
    }

    @Test
    public void testGetEncryptedPasswordWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getEncryptedPassword(userId))
            .isInstanceOf(DoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testGetEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getEncryptedPassword(badId))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getEncryptedPassword(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testDeleteEncryptedPassword() throws Exception
    {
        instance.deleteEncryptedPassword(userId);

        verify(cassandra).execute(captor.capture());

        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }
    
    @DontRepeat
    @Test
    public void testDeleteEncryptedPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteEncryptedPassword(badId))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.deleteEncryptedPassword(""))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
