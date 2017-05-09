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

 
package tech.aroma.data.memory;


import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.data.MediaRepository;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.keyInMap;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryMediaRepository implements MediaRepository
{
    private final static Logger LOG = LoggerFactory.getLogger(MemoryMediaRepository.class);
    
    private final Map<String, Image> images = Maps.createSynchronized();
    private final Map<String, Map<Dimension, Image>> thumbnails = Maps.createSynchronized();
    
    @Override
    public void saveMedia(String mediaId, Image image) throws TException
    {
        checkMediaId(mediaId);
        checkImage(image);
        
        images.put(mediaId, image);
    }

    @Override
    public Image getMedia(String mediaId) throws DoesNotExistException, TException
    {
        checkMediaId(mediaId);
        checkExists(mediaId);
        
        return images.get(mediaId);
    }

    @Override
    public void deleteMedia(String mediaId) throws TException
    {
        checkMediaId(mediaId);
        
        this.images.remove(mediaId);
    }

    @Override
    public void saveThumbnail(String mediaId, Dimension dimension, Image thumbnail) throws TException
    {
        checkMediaId(mediaId);
        checkImage(thumbnail);
        checkDimension(dimension);
        
        Map<Dimension, Image> subThumbnails = thumbnails.getOrDefault(mediaId, Maps.createSynchronized());
        subThumbnails.put(dimension, thumbnail);
        
        thumbnails.put(mediaId, subThumbnails);
    }

    @Override
    public Image getThumbnail(String mediaId, Dimension dimension) throws DoesNotExistException, TException
    {
        checkMediaId(mediaId);
        checkDimension(dimension);
        
        Map<Dimension, Image> subThumbnails = thumbnails.getOrDefault(mediaId, Maps.create());
        
        checkThat(dimension)
            .throwing(DoesNotExistException.class)
            .usingMessage("Thumbnail does not exist")
            .is(keyInMap(subThumbnails));
        
        return subThumbnails.get(dimension);
    }

    @Override
    public void deleteThumbnail(String mediaId, Dimension dimension) throws TException
    {
        checkMediaId(mediaId);
        checkDimension(dimension);
        
        Map<Dimension, Image> subThumbnails = thumbnails.getOrDefault(mediaId, Maps.create());
        
        subThumbnails.remove(dimension);
        thumbnails.put(mediaId, subThumbnails);
    }

    @Override
    public void deleteAllThumbnails(String mediaId) throws TException
    {
        checkMediaId(mediaId);
        
        thumbnails.remove(mediaId);
    }

    private void checkImage(Image image) throws InvalidArgumentException
    {
        checkThat(image)
            .throwing(InvalidArgumentException.class)
            .usingMessage("image cannot be null")
            .is(notNull());
        
        checkThat(image.getData())
            .throwing(InvalidArgumentException.class)
            .usingMessage("Image missing data")
            .is(notNull());
        
        checkThat(image.getData().length)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Image data is empty")
            .is(greaterThan(0));
        
        checkThat(image.imageType)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Image missing Type")
            .is(notNull());
            
    }

    private void checkMediaId(String mediaId) throws InvalidArgumentException
    {
        checkThat(mediaId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("mediaId missing")
            .is(nonEmptyString())
            .usingMessage("mediaId must be a UUID")
            .is(validUUID());
    }

    private void checkExists(String mediaId) throws DoesNotExistException, InvalidArgumentException
    {
        checkThat(mediaId)
            .throwing(InvalidArgumentException.class)
            .is(nonEmptyString())
            .throwing(DoesNotExistException.class)
            .is(keyInMap(this.images));
    }

    private void checkDimension(Dimension dimension) throws InvalidArgumentException
    {
        checkThat(dimension)
            .usingMessage("dimension missing")
            .is(notNull());
        
        checkThat(dimension.width)
            .usingMessage("dimension width must be > 0")
            .throwing(InvalidArgumentException.class)
            .is(greaterThan(0));
        
        checkThat(dimension.height)
            .usingMessage("dimension height must be > 0")
            .throwing(InvalidArgumentException.class)
            .is(greaterThan(0));
    }

}
