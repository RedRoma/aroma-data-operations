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

package tech.aroma.data.sql

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.MediaRepository
import tech.aroma.data.assertions.RequestAssertions.validImage
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Dimension
import tech.aroma.thrift.Image
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.aroma.thrift.exceptions.InvalidArgumentException
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID
import javax.inject.Inject


/**
 *
 * @author SirWellington
 */
internal class SQLMediaRepository
@Inject constructor(val database: JdbcOperations, val serializer: DatabaseSerializer<Image>) : MediaRepository
{
    override fun saveMedia(mediaId: String, image: Image)
    {
        checkMediaId(mediaId)
        checkImage(image)

        val sql = Inserts.MEDIA

        try
        {
            database.update(sql,
                            mediaId.toUUID(),
                            image.imageType?.toString(),
                            image.dimension?.width,
                            image.dimension?.height,
                            image.getData())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not save image", ex)
        }
    }

    override fun getMedia(mediaId: String): Image
    {
        checkMediaId(mediaId)

        val sql = Queries.SELECT_MEDIA

        return try
        {
            database.queryForObject(sql, serializer, mediaId.toUUID())
        }
        catch (ex: EmptyResultDataAccessException)
        {
            doesNotExist(mediaId, ex = ex)
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not extract Media with ID [$mediaId]", ex = ex)
        }
    }


    override fun deleteMedia(mediaId: String)
    {
        checkMediaId(mediaId)

        val sql = Deletes.MEDIA

        try
        {
            database.update(sql, mediaId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not delete Media with ID [$mediaId]", ex = ex)
        }
    }

    override fun saveThumbnail(mediaId: String, dimension: Dimension, thumbnail: Image)
    {
        checkMediaId(mediaId)
        checkImage(thumbnail)
        checkDimensions(dimension)

        val sql = Inserts.MEDIA_THUMBNAIL

        try
        {
            database.update(sql,
                            mediaId.toUUID(),
                            dimension.width,
                            dimension.height,
                            thumbnail.imageType?.toString(),
                            thumbnail.getData())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not save Thumbnail: [$mediaId -$dimension", ex)
        }
    }


    override fun getThumbnail(mediaId: String, dimension: Dimension): Image
    {
        checkMediaId(mediaId)
        checkDimensions(dimension)

        val sql = Queries.SELECT_MEDIA_THUMBNAIL

        return try
        {
            database.queryForObject(sql, serializer,
                                    mediaId.toUUID(),
                                    dimension.width,
                                    dimension.height)
        }
        catch (ex: EmptyResultDataAccessException)
        {
            doesNotExist(mediaId, dimension, ex)
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not get thumbnail: [$mediaId/$dimension]", ex)
        }
    }

    override fun deleteThumbnail(mediaId: String, dimension: Dimension)
    {
        checkMediaId(mediaId)
        checkDimensions(dimension)

        val sql = Deletes.MEDIA_THUMBNAIL

        try
        {
            database.update(sql, mediaId.toUUID(), dimension.width, dimension.height)
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not Delete thumbnail: [$mediaId | $dimension]", ex = ex)
        }
    }

    override fun deleteAllThumbnails(mediaId: String)
    {
        checkMediaId(mediaId)

        val sql = Deletes.ALL_MEDIA_THUMBNAILS

        try
        {
            database.update(sql, mediaId.toUUID())
        }
        catch (ex: Exception)
        {
            failWithMessage("Could not delete all thumbnails for [$mediaId]", ex = ex)
        }
    }

    private fun checkMediaId(id: String)
    {
        checkThat(id)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("invalid Media ID: [$id]")
                .`is`(validUUID())
    }

    private fun checkImage(image: Image?)
    {
        checkThat(image)
                .throwing(InvalidArgumentException::class.java)
                .`is`(validImage())
    }

    private fun checkDimensions(dimension: Dimension)
    {
        checkThat(dimension.width)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("invalid width")
                .`is`(positiveInteger())

        checkThat(dimension.height)
                .throwing(InvalidArgumentException::class.java)
                .usingMessage("invalid height")
                .`is`(positiveInteger())
    }

    private fun doesNotExist(mediaId: String, dimension: Dimension? = null, ex: Exception): Nothing
    {
        var message = "Media item with ID [$mediaId]"

        if (dimension != null)
        {
            message += " and dimension $dimension"
        }

        message += " does not exist"

        LOG.warn(message, ex)
        throw DoesNotExistException("$message | ${ex.message}")
    }

    private companion object
    {
        @JvmStatic val LOG = LoggerFactory.getLogger(this::class.java)
    }
}