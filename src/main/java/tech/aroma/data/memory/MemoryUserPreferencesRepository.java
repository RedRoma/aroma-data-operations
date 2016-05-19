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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;

import static java.util.stream.Collectors.toSet;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;

/**
 *
 * @author SirWellington
 */
@Internal
@ThreadSafe
final class MemoryUserPreferencesRepository implements UserPreferencesRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryUserPreferencesRepository.class);
    
    private final Map<String, Set<MobileDevice>> devices = Maps.createSynchronized();
    
    @Override
    public void saveMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        checkUserId(userId);
        checkMobileDevice(mobileDevice);
        
        Set<MobileDevice> userDevices = devices.getOrDefault(userId, Sets.create());
        userDevices.add(mobileDevice);
        devices.put(userId, userDevices);
    }
    
    @Override
    public void saveMobileDevices(String userId, Set<MobileDevice> mobileDevices) throws TException
    {
        checkUserId(userId);
        checkDevices(mobileDevices);
        
        devices.put(userId, mobileDevices);
    }
    
    @Override
    public Set<MobileDevice> getMobileDevices(String userId) throws TException
    {
        checkUserId(userId);
        
        return devices.getOrDefault(userId, Sets.emptySet());
    }
    
    @Override
    public void deleteMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        checkUserId(userId);
        checkMobileDevice(mobileDevice);
        
        Set<MobileDevice> filteredDevices = devices.getOrDefault(userId, Sets.emptySet())
            .stream()
            .filter(device -> Objects.equals(device, mobileDevice) == false)
            .collect(toSet());
        
        devices.put(userId, filteredDevices);
    }
    
    @Override
    public void deleteAllMobileDevices(String userId) throws TException
    {
        checkUserId(userId);
        
        devices.remove(userId);
    }
    
    private void checkMobileDevice(MobileDevice mobileDevice) throws InvalidArgumentException
    {
        checkThat(mobileDevice)
            .throwing(InvalidArgumentException.class)
            .usingMessage("MobileDevice cannot be null")
            .is(notNull());
        
        checkThat(mobileDevice.isSet())
            .throwing(InvalidArgumentException.class)
            .usingMessage("Mobiel Device must be set")
            .is(trueStatement());
    }
    
    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }
    
    private void checkDevices(Set<MobileDevice> mobileDevices) throws InvalidArgumentException
    {
        checkThat(mobileDevices)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Set of Mobile Devices cannot be null")
            .is(notNull());
        
        for (MobileDevice device : mobileDevices)
        {
            checkMobileDevice(device);
        }
    }
    
}
