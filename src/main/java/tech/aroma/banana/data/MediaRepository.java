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


package tech.aroma.banana.data;

import org.apache.thrift.TException;
import tech.aroma.banana.thrift.Dimension;
import tech.aroma.banana.thrift.Image;
import tech.aroma.banana.thrift.exceptions.DoesNotExistException;
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
    
    void deleteMedia(@NonEmpty String mediaId) throws TException;
    
    void saveThumbnail(@NonEmpty String mediaId, @Required Dimension dimension, Image thumbnail) throws TException;
    
    Image getThumbnail(@NonEmpty String mediaId, @Required Dimension dimension) throws DoesNotExistException, TException;
    
    void deleteThumbnail(@NonEmpty String mediaId, @Required Dimension dimension) throws TException;
    
    void deleteAllThumbnails(@NonEmpty String mediaId) throws TException;
    
}
