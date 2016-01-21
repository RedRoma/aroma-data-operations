/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.data.cassandra;

import com.datastax.driver.core.Row;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MappersTest
{

    @Mock
    private Row row;

    @GeneratePojo
    private Application app;

    @GeneratePojo
    private Message message;

    @GeneratePojo
    private User user;

    @GeneratePojo
    private Organization org;

    @Before
    public void setUp()
    {
    }

    @Test
    @DontRepeat
    public void testCannotInstantiate()
    {
        assertThrows(() -> new Mappers())
            .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testAppMapper()
    {
        Function<Row, Application> mapper = Mappers.appMapper();
        assertThat(mapper, notNullValue());

    }

    @Test
    public void testMessageMapper()
    {
        Function<Row, Message> mapper = Mappers.messageMapper();
        assertThat(mapper, notNullValue());
    }

    @Test
    public void testOrgMapper()
    {
        Function<Row, Organization> mapper = Mappers.orgMapper();
        assertThat(mapper, notNullValue());
    }

    @Test
    public void testUserMapper()
    {
        Function<Row, User> mapper = Mappers.userMapper();
        assertThat(mapper, notNullValue());
    }

}
