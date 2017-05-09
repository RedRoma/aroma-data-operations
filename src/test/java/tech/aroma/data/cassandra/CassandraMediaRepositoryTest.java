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

import java.util.function.Function;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CassandraMediaRepositoryTest
{

    @Mock
    private Session cassandra;

    @Mock
    private Function<Row, Image> imageMapper;

    private CassandraMediaRepository instance;
    
    @GenerateString(UUID)
    private String mediaId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    @GeneratePojo
    private Image image;
    
    @GeneratePojo
    private Dimension dimension;
    
    @Captor
    private ArgumentCaptor<Statement> captor;
    
    @Mock
    private ResultSet results;
    
    @Mock
    private Row row;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new CassandraMediaRepository(cassandra, imageMapper);
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {
        when(results.one()).thenReturn(row);
        when(imageMapper.apply(row)).thenReturn(image);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CassandraMediaRepository(null,  imageMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new CassandraMediaRepository(cassandra, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSaveMedia() throws Exception
    {
        instance.saveMedia(mediaId, image);
        
        verify(cassandra).execute(captor.capture());
        
        Statement statement = captor.getValue();
        assertThat(statement, notNullValue());
        assertThat(statement, is(instanceOf(Insert.class)));
    }
    
    @DontRepeat
    @Test
    public void testSaveMediaWithBadArgs() throws Exception
    {
        //Missing media id
        assertThrows(() -> instance.saveMedia("", image))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Missing Image
        assertThrows(() -> instance.saveMedia(mediaId, null))
            .isInstanceOf(InvalidArgumentException.class);
        
        //bad Id
        assertThrows(() -> instance.saveMedia(badId, image))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Empty Image
        Image emptyImage = new Image();
        assertThrows(() -> instance.saveMedia(badId, emptyImage))
            .isInstanceOf(InvalidArgumentException.class);
        
    }

    @Test
    public void testGetMedia() throws Exception
    {
    }

    @Test
    public void testDeleteMedia() throws Exception
    {
    }

    @Test
    public void testSaveThumbnail() throws Exception
    {
    }

    @Test
    public void testGetThumbnail() throws Exception
    {
    }

    @Test
    public void testDeleteThumbnail() throws Exception
    {
    }

    @Test
    public void testDeleteAllThumbnails() throws Exception
    {
    }

}
