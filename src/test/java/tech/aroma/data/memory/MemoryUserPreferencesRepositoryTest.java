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

package tech.aroma.data.memory;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHANUMERIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryUserPreferencesRepositoryTest
{
    
    @GenerateString(ALPHANUMERIC)
    private String badId;
    
    @GenerateString(UUID)
    private String userId;
    
    private MobileDevice device;
    private Set<MobileDevice> devices;
    private MobileDevice emptyDevice;
    
    private MemoryUserPreferencesRepository instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new MemoryUserPreferencesRepository();
        
        setupData();
        setupMocks();
    }
    
    private void setupData() throws Exception
    {
        device = one(mobileDevices());
        devices = Sets.copyOf(listOf(mobileDevices(), 10));
        emptyDevice = new MobileDevice();
    }
    
    private void setupMocks() throws Exception
    {
        
    }
    
    @Test
    public void testSaveMobileDevice() throws Exception
    {
        instance.saveMobileDevice(userId, device);
        assertThat(instance.containsMobileDevice(userId, device), is(true));
    }
    
    @Test
    public void testSaveMobileDeviceWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveMobileDevice("", device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevice(badId, device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevice(userId, null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevice(userId, emptyDevice)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testSaveMobileDevices() throws Exception
    {
        
    }
    
    @Test
    public void testSaveMobileDevicesWithBadArgs() throws Exception
    {
        Set<MobileDevice> emptyDevices = Sets.createFrom(new MobileDevice());
        
        assertThrows(() -> instance.saveMobileDevices("", devices)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(badId, devices)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(userId, null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.saveMobileDevices(userId, emptyDevices)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testGetMobileDevices() throws Exception
    {
        instance.saveMobileDevices(userId, devices);
        
        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, is(devices));
    }
    
    @Test
    public void testGetMobileDevicesWhenNoDevics() throws Exception
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
        instance.saveMobileDevice(userId, device);
        assertThat(instance.containsMobileDevice(userId, device), is(true));

        instance.deleteMobileDevice(userId, device);
        assertThat(instance.containsMobileDevice(userId, device), is(false));
    }
    
    @Test
    public void testDeleteMobileDeviceWhenDoesNotExist() throws Exception
    {
        instance.saveMobileDevice(userId, device);
        
        MobileDevice newDevice = one(mobileDevices());
        instance.deleteMobileDevice(userId, newDevice);
    }
    
    @Test
    public void testDeleteMobileDeviceWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteMobileDevice("", device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(badId, device)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(userId, null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteMobileDevice(userId, emptyDevice)).isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testDeleteAllMobileDevices() throws Exception
    {
        instance.saveMobileDevices(userId, devices);
        for (MobileDevice d : devices)
        {
            assertThat(instance.containsMobileDevice(userId, d), is(true));
        }
        
        
        instance.deleteAllMobileDevices(userId);
        for (MobileDevice d : devices)
        {
            assertThat(instance.containsMobileDevice(userId, d), is(false));
        }
        
        Set<MobileDevice> result = instance.getMobileDevices(userId);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @Test
    public void testDeleteAllMobileDevicesWhenNoneExist() throws Exception
    {
        instance.deleteAllMobileDevices(userId);
    }
    
    @Test
    public void testDeleteAllMobileDevicesWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteAllMobileDevices("")).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.deleteAllMobileDevices(badId)).isInstanceOf(InvalidArgumentException.class);
    }
    
}
