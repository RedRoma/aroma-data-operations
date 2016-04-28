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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryMediaRepositoryTest 
{
    @GeneratePojo
    private Image image;
    
    @GenerateList(Image.class)
    private List<Image> thumbnails;
    
    @GenerateString(UUID)
    private String mediaId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private MemoryMediaRepository instance;

    
    @Before
    public void setUp() throws Exception
    {
        instance = new MemoryMediaRepository();
        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testSaveMedia() throws Exception
    {
        instance.saveMedia(mediaId, image);
        assertThat(instance.containsMedia(mediaId), is(true));
    }

    @Test
    public void testGetMedia() throws Exception
    {
        instance.saveMedia(mediaId, image);
        
        Image result = instance.getMedia(mediaId);
        assertThat(result, is(image));
    }
    
    @Test
    public void testGetMediaWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getMedia(mediaId))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testDeleteMedia() throws Exception
    {
        instance.saveMedia(mediaId, image);
        
        instance.deleteMedia(mediaId);
        
        assertThat(instance.containsMedia(mediaId), is(false));
    }
        
    public void testDeleteMediaWhenNotExists() throws Exception
    {
        instance.deleteMedia(mediaId);
    }

    @Test
    public void testSaveThumbnail() throws Exception
    {
        Image thumbnail = Lists.oneOf(thumbnails);
        Dimension dimension = thumbnail.getDimension();
        instance.saveThumbnail(mediaId, dimension, thumbnail);
        
        assertThat(instance.containsThumbnail(mediaId, dimension), is(true));
        
    }

    @Test
    public void testGetThumbnail() throws Exception
    {
        Image thumbnail = Lists.oneOf(thumbnails);
        Dimension dimension = thumbnail.getDimension();
        instance.saveThumbnail(mediaId, dimension, thumbnail);
        
        Image result = instance.getThumbnail(mediaId, dimension);
        assertThat(result, is(thumbnail));
    }

    @Test
    public void testGetThumbnailWhenNotExists() throws Exception
    {
        assertThrows(() -> instance.getThumbnail(mediaId, image.getDimension()))
            .isInstanceOf(DoesNotExistException.class);
    }
    
    @Test
    public void testDeleteThumbnail() throws Exception
    {
        Image thumbnail = Lists.oneOf(thumbnails);
        Dimension dimension = thumbnail.getDimension();
        instance.saveThumbnail(mediaId, dimension, thumbnail);
        
        instance.deleteThumbnail(mediaId, dimension);
        
        assertThat(instance.containsThumbnail(mediaId, dimension), is(false));
    }
    
    @Test
    public void testDeleteThumbnailWhenNotExists() throws Exception
    {
        instance.deleteThumbnail(mediaId, image.getDimension());
    }

    @Test
    public void testDeleteAllThumbnails() throws Exception
    {
        
        for (Image thumbnail : thumbnails)
        {
            Dimension dimension = thumbnail.getDimension();
            instance.saveThumbnail(mediaId, dimension, thumbnail);
            assertThat(instance.containsThumbnail(mediaId, dimension), is(true));
        }
        
        instance.deleteAllThumbnails(mediaId);
        
        assertThat(instance.containsThumbnail(mediaId, image.getDimension()), is(false));
    }

}
