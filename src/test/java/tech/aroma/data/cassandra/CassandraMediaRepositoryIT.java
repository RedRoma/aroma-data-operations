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

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import java.util.List;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class CassandraMediaRepositoryIT
{

    private static Session session;

    @BeforeClass
    public static void begin()
    {
        session = TestCassandraProviders.getTestSession();
    }

    @GenerateString(UUID)
    private String mediaId;

    @GeneratePojo
    private Image image;
    
    @GeneratePojo
    private Dimension dimension;
    
    @GenerateList(Image.class)
    private List<Image> thumbnails;

    private Function<Row, Image> imageMapper = Mappers.imageMapper();

    private CassandraMediaRepository instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new CassandraMediaRepository(session, imageMapper);

        setupData();
        setupMocks();
    }

    @After
    public void cleanUp() throws TException
    {
        try
        {
            instance.deleteMedia(mediaId);
            instance.deleteAllThumbnails(mediaId);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to delete media: " + mediaId + " | " + ex.getMessage());
        }
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
        assertImagesTheSame(result, image);
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

    @Test
    public void testSaveThumbnail() throws Exception
    {
        instance.saveThumbnail(mediaId, dimension, image);
        
        assertThat(instance.containsThumbnail(mediaId, dimension), is(true));
    }

    @Test
    public void testGetThumbnail() throws Exception
    {
        instance.saveThumbnail(mediaId, dimension, image);
        
        Image result = instance.getThumbnail(mediaId, dimension);

        assertThat(result, notNullValue());
        assertThat(result.data, is(image.data));
        assertThat(result.imageType, is(image.imageType));
    }

    @Test
    public void testDeleteThumbnail() throws Exception
    {
        instance.saveThumbnail(mediaId, dimension, image);
        
        instance.deleteThumbnail(mediaId, dimension);
        
        assertThat(instance.containsThumbnail(mediaId, dimension), is(false));
    }

    @Test
    public void testDeleteAllThumbnails() throws Exception
    {
        for(Image thumbnail : thumbnails)
        {
            Dimension dimension = thumbnail.dimension;
            
            instance.saveThumbnail(mediaId, dimension, thumbnail);
        }
        
        instance.deleteAllThumbnails(mediaId);
        
        Image oneThumbnail = Lists.oneOf(thumbnails);
        assertThat(instance.containsThumbnail(mediaId, oneThumbnail.dimension), is(false));
    }

    private void assertImagesTheSame(Image result, Image image)
    {
        assertThat(result, notNullValue());
        assertThat(result.data, is(image.data));
        assertThat(result.imageType, is(image.imageType));
        assertThat(result.dimension, is(image.dimension));
    }

}
