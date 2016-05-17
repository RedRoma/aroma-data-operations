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

import java.util.Set;
import org.apache.thrift.TException;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.thrift.User;
import tech.aroma.thrift.channels.MobileDevice;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * This Repository is responsible for storing User Devices. This is useful for sending Push Notifications
 * to Aroma Users.
 * 
 * @author SirWellington
 */
public interface DeviceRepository 
{
    default boolean containsMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        Set<MobileDevice> devices = getMobileDevices(userId);
        
        if (Sets.isEmpty(devices))
        {
            return false;
        }
        
        return devices.contains(mobileDevice);
    }
    
    void saveMobileDevice(@NonEmpty String userId, @Required MobileDevice mobileDevice) throws TException;
    
    /**
     * This Operation overwrites all existing Mobile Devices registered for the specified User.
     * 
     * @param userId The {@linkplain User#userId User ID} to save and associate the devices with.
     * @param mobileDevices The Set of Mobile Devices belonging to the user.
     * @throws TException 
     */
    void saveMobileDevices(@NonEmpty String userId, @Required Set<MobileDevice> mobileDevices) throws TException;
    
    /**
     * Get the Set of all Mobile Devices registered to a {@link User}.
     * 
     * @param userId The {@linkplain User#userId User ID} of the User.
     * @return
     * @throws TException 
     */
    Set<MobileDevice> getMobileDevices(@NonEmpty String userId) throws TException;
    
    /**
     * Disassociates and removes a device associated with a particular user.
     * 
     * @param userId The User ID the Device is associated with.
     * @param mobileDevice The Device to remove.
     * @throws TException 
     */
    void deleteMobileDevice(@NonEmpty String userId, @Required MobileDevice mobileDevice) throws TException;
    
    /**
     * Removes an Disassociates all devices registered to the specified {@Link User}.
     * 
     * @param userId
     * @throws TException 
     */
    void deleteAllMobileDevice(@NonEmpty String userId) throws TException;
}
