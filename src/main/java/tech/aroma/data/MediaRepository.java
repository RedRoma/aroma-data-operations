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


package tech.aroma.data;

import org.apache.thrift.TException;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 *
 * Allows storage of binary media types like Images and Videos.
 * 
 * @author SirWellington
 */
public interface MediaRepository 
{
    
    void saveMedia(@NonEmpty String mediaId, @Required Image image) throws TException;
    
    Image getMedia(@NonEmpty String mediaId) throws DoesNotExistException, TException;
    
    default boolean containsMedia(@NonEmpty String mediaId) throws TException
    {
        try
        {
            Image result = this.getMedia(mediaId);
            return result != null;
        }
        catch (DoesNotExistException ex)
        {
            return false;
        }
    }
    
    void deleteMedia(@NonEmpty String mediaId) throws TException;
    
    void saveThumbnail(@NonEmpty String mediaId, @Required Dimension dimension, Image thumbnail) throws TException;
    
    Image getThumbnail(@NonEmpty String mediaId, @Required Dimension dimension) throws DoesNotExistException, TException;
    
    default boolean containsThumbnail(@NonEmpty String mediaId, @Required Dimension dimension) throws TException
    {
        try
        {
            Image thumbnail = this.getThumbnail(mediaId, dimension);
            return thumbnail != null;
        }
        catch(DoesNotExistException ex)
        {
            return false;
        }
    }
    
    void deleteThumbnail(@NonEmpty String mediaId, @Required Dimension dimension) throws TException;
    
    void deleteAllThumbnails(@NonEmpty String mediaId) throws TException;
    
}
