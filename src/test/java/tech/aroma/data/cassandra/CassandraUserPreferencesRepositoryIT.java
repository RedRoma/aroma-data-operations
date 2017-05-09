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

import java.util.Set;
import java.util.function.Function;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.*;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.channels.MobileDevice;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraUserPreferencesRepositoryIT
{

    private static Session session;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
    }

    @GenerateString(UUID)
    private String userId;

    Function<Row, Set<MobileDevice>> mapper = Mappers.mobileDeviceMapper();

    private CassandraUserPreferencesRepository instance;

    private MobileDevice device;
    private Set<MobileDevice> devices;

    @Before
    public void setUp() throws Exception
    {
        setupData();

        instance = new CassandraUserPreferencesRepository(session, mapper);
    }

    @After
    public void cleanUp() throws Exception
    {
        instance.deleteAllMobileDevices(userId);
    }

    private void setupData() throws Exception
    {

        device = one(mobileDevices());
        devices = Sets.copyOf(listOf(mobileDevices(), 10));
    }

    @Test
    public void testSaveMobileDevice() throws Exception
    {
        instance.saveMobileDevice(userId, device);

        assertThat(instance.containsMobileDevice(userId, device), is(true));
    }

    @Test
    public void testSaveMobileDevices() throws Exception
    {
        instance.saveMobileDevices(userId, devices);

        for (MobileDevice device : devices)
        {
            assertThat(instance.containsMobileDevice(userId, device), is(true));
        }

        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, is(devices));
    }

    @Test
    public void testGetMobileDevices() throws Exception
    {
        instance.saveMobileDevices(userId, devices);

        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, is(devices));
    }

    @Test
    public void testGetMobileDevicesWhenOne() throws Exception
    {
        instance.saveMobileDevice(userId, device);
        Set<MobileDevice> expected = Sets.createFrom(device);

        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, is(expected));
    }

    @Test
    public void testGetMobileDevicesWhenEmpty() throws Exception
    {
        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    public void testDeleteMobileDevice() throws Exception
    {
        instance.saveMobileDevice(userId, device);

        instance.deleteMobileDevice(userId, device);
        assertThat(instance.containsMobileDevice(userId, device), is(false));
    }

    @Test
    public void testDeleteMobileDeviceWhenDoesNotExist() throws Exception
    {
        instance.deleteMobileDevice(userId, device);
    }

    @Test
    public void testDeleteAllMobileDevices() throws Exception
    {
        instance.saveMobileDevice(userId, device);
        instance.saveMobileDevices(userId, devices);

        instance.deleteAllMobileDevices(userId);
        assertThat(instance.containsMobileDevice(userId, device), is(false));
        for (MobileDevice mobileDevice : devices)
        {
            assertThat(instance.containsMobileDevice(userId, mobileDevice), is(false));
        }

        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

}
