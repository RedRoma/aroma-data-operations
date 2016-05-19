/*
 * Copyright 2016 RedRoma, Inc..
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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Update;
import java.util.Set;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class CassandraUserPreferencesRepositoryTest
{
    
    @Mock
    private Session cassandra;
    
    @Mock
    private Function<Row, Set<MobileDevice>> mobileDeviceMapper;
    
    @Mock
    private ResultSet results;
    
    @Mock
    private Row row;
    
    @Captor
    private ArgumentCaptor<Statement> captor;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    @GenerateString(UUID)
    private String userId;
    
    private MobileDevice device;
    private MobileDevice badDevice;
    private Set<MobileDevice> devices;
    
    private CassandraUserPreferencesRepository instance;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new CassandraUserPreferencesRepository(cassandra, mobileDeviceMapper);
        verifyZeroInteractions(cassandra, mobileDeviceMapper);
    }
    
    private void setupData() throws Exception
    {
        device = one(mobileDevices());
        badDevice = new MobileDevice();
        devices = Sets.toSet(listOf(mobileDevices(), 10));
    }
    
    private void setupMocks() throws Exception
    {
        when(cassandra.execute(any(Statement.class))).thenReturn(results);
        when(results.one()).thenReturn(row);
        when(mobileDeviceMapper.apply(row)).thenReturn(Sets.create());
        
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraUserPreferencesRepository(null, mobileDeviceMapper));
        assertThrows(() -> new CassandraUserPreferencesRepository(cassandra, null));
    }
    
    @Test
    public void testSaveMobileDevice() throws Exception
    {
        instance.saveMobileDevice(userId, device);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Update.Where.class)));
    }
    
    @Test
    public void testSaveMobileDeviceWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveMobileDevice(userId, badDevice)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevice(userId, badDevice)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevice(userId, badDevice)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testSaveMobileDevices() throws Exception
    {
        instance.saveMobileDevices(userId, devices);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.class)));
    }
    
    @Test
    public void testSaveMobileDevicesWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveMobileDevices("", devices)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(badId, devices)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(userId, null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(userId, Sets.createFrom(badDevice))).isInstanceOf(
            InvalidArgumentException.class);
    }
    
    @Test
    public void testGetMobileDevices() throws Exception
    {
        when(mobileDeviceMapper.apply(row)).thenReturn(devices);
        
        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, is(devices));
    }
    
    @Test
    public void testGetMobileDevicesWhenNone() throws Exception
    {
        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testGetMobileDevicesWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getMobileDevices("")).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.getMobileDevices(badId)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testDeleteMobileDevice() throws Exception
    {
        instance.deleteMobileDevice(userId, device);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Update.Where.class)));
    }
    
    @Test
    public void testDeleteMobileDeviceWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteMobileDevice("", device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(badId, device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(userId, null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(userId, badDevice)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testDeleteAllMobileDevices() throws Exception
    {
        instance.deleteAllMobileDevices(userId);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Delete.Where.class)));
    }
    
    @Test
    public void testDeleteAllMobileDevicesWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllMobileDevices("")).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteAllMobileDevices(badId)).isInstanceOf(InvalidArgumentException.class);
    }
    
}
