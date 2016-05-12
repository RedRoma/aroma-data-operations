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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.cassandra.Tables.Media;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class CassandraMediaRepository implements MediaRepository
{
    
    private final static Logger LOG = LoggerFactory.getLogger(CassandraMediaRepository.class);
    
    private final Session cassandra;
    private final Function<Row, Image> imageMapper;
    
    @Inject
    CassandraMediaRepository(Session cassandra, Function<Row, Image> imageMapper)
    {
        checkThat(cassandra, imageMapper)
            .are(notNull());
        
        this.cassandra = cassandra;
        this.imageMapper = imageMapper;
    }
    
    @Override
    public void saveMedia(String mediaId, Image image) throws TException
    {
        checkMediaId(mediaId);
        checkImage(image);
        
        Statement insertStatement = createStatementToSaveImage(mediaId, image);
        
        tryToExecute(insertStatement, "Could not save Media in Cassandra: " + mediaId);
        
        LOG.debug("Successfully saved media with ID {} in Cassandra", mediaId);
    }
    
    @Override
    public Image getMedia(String mediaId) throws DoesNotExistException, TException
    {
        checkMediaId(mediaId);
        
        Statement query = createQueryToGetImage(mediaId);
        
        ResultSet results = tryToExecute(query, "Could not query Cassandra for Media with ID:" + mediaId);
        checkResultsNotMissing(results);
        
        Row row = results.one();
        ensureRowExists(row, mediaId);
        
        Image image = converRowToImage(row);
        return image;
    }
    
    @Override
    public void deleteMedia(String mediaId) throws TException
    {
        checkMediaId(mediaId);
        
        Statement deleteStatement = createStatementToDeleteMedia(mediaId);
        
        tryToExecute(deleteStatement, "Failed to delete media from Cassandra: " + mediaId);
    }
    
    @Override
    public void saveThumbnail(String mediaId, Dimension dimension, Image thumbnail) throws TException
    {
        checkMediaId(mediaId);
        checkImage(thumbnail);
        checkDimension(dimension);
        
        Statement insertStatement = createStatementToSaveThumbnail(mediaId, dimension, thumbnail);
        tryToExecute(insertStatement, "Failed to save Thumbnail in Cassandra. Media ID: " + mediaId + " Dimension: " + dimension);
    }
    
    @Override
    public Image getThumbnail(String mediaId, Dimension dimension) throws DoesNotExistException, TException
    {
        checkMediaId(mediaId);
        checkDimension(dimension);
        
        Statement query = createQueryToGetThumbnail(mediaId, dimension);
        
        ResultSet results = tryToExecute(query,
                                         "Failed to Query Cassandra for Thumbnail with Media ID: " + mediaId + " Dimension: " + dimension);
        checkResultsNotMissing(results);
        
        Row row = results.one();
        ensureRowExists(row, mediaId);
        
        Image thumbnail = converRowToImage(row);
        return thumbnail;
    }
    
    @Override
    public void deleteThumbnail(String mediaId, Dimension dimension) throws TException
    {
        checkMediaId(mediaId);
        checkDimension(dimension);
        
        Statement deleteStatement = createStatementToDeleteThubmnail(mediaId, dimension);
        
        tryToExecute(deleteStatement, "Failed to delete Thumbnail from Cassandra: " + mediaId + " Dimension: " + dimension);
    }
    
    @Override
    public void deleteAllThumbnails(String mediaId) throws TException
    {
        checkMediaId(mediaId);
        
        Statement deleteStatement = createStatementToDeleteAllThumbnailsFor(mediaId);
        
        tryToExecute(deleteStatement, "Failed to delete all Thumbnails belonging to Media: " + mediaId);
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
    
    private void checkImage(Image image) throws InvalidArgumentException
    {
        checkThat(image)
            .throwing(InvalidArgumentException.class)
            .usingMessage("image cannot be null")
            .is(notNull());
        
        byte[] data = image.getData();
        checkThat(data)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Image is missing data")
            .is(notNull());
        
        checkThat(data.length)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Image data is empty")
            .is(greaterThan(0));
    }
    
    private ResultSet tryToExecute(Statement statement, String errorMessage) throws OperationFailedException
    {
        try
        {
            return cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute Cassandra Statement: {}", errorMessage, ex);
            throw new OperationFailedException(errorMessage + " | " + ex.getMessage());
        }
    }
    
    private Statement createStatementToSaveImage(String mediaId, Image image)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        String type = image.imageType != null ? image.imageType.toString() : null;
        Dimension dimension = image.dimension != null ? image.dimension : new Dimension();
        
        return QueryBuilder
            .insertInto(Tables.Media.TABLE_NAME)
            .value(Media.MEDIA_ID, mediaUuid)
            .value(Media.MEDIA_TYPE, type)
            .value(Media.CREATION_TIME, Instant.now().toEpochMilli())
            .value(Media.BINARY, image.data)
            .value(Media.WIDTH, dimension.width)
            .value(Media.HEIGHT, dimension.height);
    }
    
    private Statement createQueryToGetImage(String mediaId)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        
        return QueryBuilder
            .select()
            .all()
            .from(Tables.Media.TABLE_NAME)
            .where(eq(Media.MEDIA_ID, mediaUuid));
    }
    
    private void checkResultsNotMissing(ResultSet results) throws OperationFailedException
    {
        checkThat(results)
            .throwing(OperationFailedException.class)
            .usingMessage("Cassandra returned null ResultSet")
            .is(notNull());
    }
    
    private void ensureRowExists(Row row, String mediaId) throws DoesNotExistException
    {
        checkThat(row)
            .throwing(DoesNotExistException.class)
            .usingMessage("Media does not exist: " + mediaId)
            .is(notNull());
    }
    
    private Image converRowToImage(Row row)
    {
        return imageMapper.apply(row);
    }
    
    private Statement createStatementToDeleteMedia(String mediaId)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Tables.Media.TABLE_NAME)
            .where(eq(Media.MEDIA_ID, mediaUuid));
    }
    
    private void checkDimension(Dimension dimension) throws InvalidArgumentException
    {
        checkThat(dimension)
            .usingMessage("dimension cannot be null")
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        checkThat(dimension.width)
            .usingMessage("dimension width must be >0")
            .throwing(InvalidArgumentException.class)
            .is(greaterThan(0));
        
        checkThat(dimension.height)
            .usingMessage("dimension height must be >0")
            .throwing(InvalidArgumentException.class)
            .is(greaterThan(0));
    }
    
    private Statement createStatementToSaveThumbnail(String mediaId, Dimension dimension, Image thumbnail)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        String dimensionString = dimension.toString();
        String mediaType = thumbnail.imageType != null ? thumbnail.imageType.toString() : null;
        
        return QueryBuilder
            .insertInto(Tables.Media.TABLE_NAME_THUMBNAILS)
            .value(Media.MEDIA_ID, mediaUuid)
            .value(Media.DIMENSION, dimensionString)
            .value(Media.WIDTH, dimension.width)
            .value(Media.HEIGHT, dimension.height)
            .value(Media.CREATION_TIME, Instant.now().toEpochMilli())
            .value(Media.BINARY, thumbnail.data)
            .value(Media.MEDIA_TYPE, mediaType);
        
    }
    
    private Statement createQueryToGetThumbnail(String mediaId, Dimension dimension)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        String dimensionString = dimension.toString();
        
        return QueryBuilder
            .select()
            .all()
            .from(Tables.Media.TABLE_NAME_THUMBNAILS)
            .where(eq(Media.MEDIA_ID, mediaUuid))
            .and(eq(Media.DIMENSION, dimensionString));
    }
    
    private Statement createStatementToDeleteThubmnail(String mediaId, Dimension dimension)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        String dimensionString = dimension.toString();
        
        return QueryBuilder
            .delete()
            .all()
            .from(Tables.Media.TABLE_NAME_THUMBNAILS)
            .where(eq(Media.MEDIA_ID, mediaUuid))
            .and(eq(Media.DIMENSION, dimensionString));
    }
    
    private Statement createStatementToDeleteAllThumbnailsFor(String mediaId)
    {
        UUID mediaUuid = UUID.fromString(mediaId);
        
        return QueryBuilder
            .delete()
            .all()
            .from(Tables.Media.TABLE_NAME_THUMBNAILS)
            .where(eq(Media.MEDIA_ID, mediaUuid));
    }
    
}
