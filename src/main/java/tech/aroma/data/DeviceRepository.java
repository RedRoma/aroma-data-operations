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


package tech.aroma.data;

import java.util.List;
import java.util.Set;
import org.apache.thrift.TException;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.channels.MobileDevice;


/**
 *
 * @author SirWellington
 */
public interface DeviceRepository 
{
    default boolean containsMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        Set<MobileDevice> devices = getMobileDevice(userId);
        
        if (Sets.isEmpty(devices))
        {
            return false;
        }
        
        return devices.contains(mobileDevice);
    }
    
    void saveMobileDevice(String userId, MobileDevice mobileDevice) throws TException;
    
    void saveMobileDevices(String userId, List<MobileDevice> mobileDevices) throws TException;
    
    Set<MobileDevice> getMobileDevice(String userId) throws TException;
    
    void deleteMovileDevice(String userId, MobileDevice mobileDevice) throws TException;
    
    void deleteAllMobileDevice(String userId) throws TException;
}
