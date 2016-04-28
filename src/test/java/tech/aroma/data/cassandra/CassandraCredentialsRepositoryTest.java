/*
 * Copyright 2016 RedRoma, Inc.
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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.data.cassandra.Tables.Credentials;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraCredentialsRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Cluster cluster;

    @Mock
    private Session cassandra;

    private QueryBuilder queryBuilder;

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

        instance = new CassandraCredentialsRepository(cassandra, queryBuilder);
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {
        queryBuilder = new QueryBuilder(cluster);

        when(results.one()).thenReturn(row);
        when(cassandra.execute(Mockito.any(Statement.class)))
            .thenReturn(results);

    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraCredentialsRepository(null, queryBuilder))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraCredentialsRepository(cassandra, null))
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
