package tech.aroma.data.sql

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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.*
import tech.aroma.data.AromaGenerators.Images
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Dimension
import tech.aroma.thrift.Image
import tech.sirwellington.alchemy.generator.AlchemyGenerator.one
import tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import java.sql.Timestamp

@RunWith(AlchemyTestRunner::class)
class SQLMediaRepositoryTest
{
    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var serializer: DatabaseSerializer<Image>

    private lateinit var image: Image
    private lateinit var thumbnail: Image

    @GeneratePojo
    private lateinit var dimension: Dimension

    @GenerateString(UUID)
    private lateinit var mediaId: String

    @GenerateString(ALPHABETIC)
    private lateinit var badId: String

    private lateinit var badImage: Image
    private lateinit var badDimension: Dimension

    private lateinit var instance: SQLMediaRepository

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()
        instance = SQLMediaRepository(database, serializer)
    }

    @Test
    fun testSaveMedia()
    {
        instance.saveMedia(mediaId, image)

        val sql = Inserts.MEDIA
        verify(database).update(eq(sql),
                                eq(mediaId.toUUID()),
                                eq(image.imageType.toString()),
                                eq(dimension.width),
                                eq(dimension.height),
                                any<Timestamp>(),
                                eq(image.getData()))
    }

    @DontRepeat
    @Test
    fun testSaveMediaWithBadArgs()
    {
        assertThrows { instance.saveMedia("", image) }.invalidArg()
        assertThrows { instance.saveMedia(badId, image) }.invalidArg()
        assertThrows { instance.saveMedia(mediaId, badImage) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testSaveMediaWhenFails()
    {
        setupForFailure()

        assertThrows { instance.saveMedia(mediaId, image) }.operationError()
    }

    @Test
    fun testGetMedia()
    {
        val sql = Queries.SELECT_MEDIA

        whenever(database.queryForObject(sql, serializer, mediaId.toUUID()))
                .thenReturn(image)

        val result = instance.getMedia(mediaId)
        assertThat(result, equalTo(image))
    }

    @DontRepeat
    @Test
    fun testGetMediaWithBadArgs()
    {
        assertThrows { instance.getMedia("") }.invalidArg()
        assertThrows { instance.getMedia(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetMediaWhenFails()
    {
        setupForFailure()
        assertThrows { instance.getMedia(mediaId) }.operationError()
    }

    @DontRepeat
    @Test
    fun testGetMediaWhenNotExists()
    {
        val sql = Queries.SELECT_MEDIA

        whenever(database.queryForObject(sql, serializer, mediaId.toUUID()))
                .thenThrow(EmptyResultDataAccessException(0))

        assertThrows { instance.getMedia(mediaId) }.doesNotExist()
    }

    @Test
    fun testDeleteMedia()
    {
        val sql = Deletes.MEDIA
        instance.deleteMedia(mediaId)

        verify(database).update(sql, mediaId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteMediaWithBadArgs()
    {
        assertThrows { instance.deleteMedia("") }.invalidArg()
        assertThrows { instance.deleteMedia(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteMediaWhenFails()
    {
        setupForFailure()
        assertThrows { instance.deleteMedia(mediaId) }.operationError()
    }

    @Test
    fun testSaveThumbnail()
    {
        val sql = Inserts.MEDIA_THUMBNAIL

        instance.saveThumbnail(mediaId, dimension, thumbnail)

        verify(database).update(sql,
                                mediaId.toUUID(),
                                dimension.width,
                                dimension.height,
                                thumbnail.imageType.toString(),
                                thumbnail.getData())
    }

    @DontRepeat
    @Test
    fun testSaveThumbnailWithBadArgs()
    {
        assertThrows { instance.saveThumbnail("", dimension, thumbnail) }.invalidArg()
        assertThrows { instance.saveThumbnail(badId, dimension, thumbnail) }.invalidArg()
        assertThrows { instance.saveThumbnail(mediaId, badDimension, thumbnail) }.invalidArg()
        assertThrows { instance.saveThumbnail(mediaId, dimension, badImage) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testSaveThumbnailWhenFails()
    {
        setupForFailure()
        assertThrows { instance.saveThumbnail(mediaId, dimension, thumbnail) }
                .operationError()
    }

    @Test
    fun testGetThumbnail()
    {
        val sql = Queries.SELECT_MEDIA_THUMBNAIL

        whenever(database.queryForObject(sql, serializer,
                                         mediaId.toUUID(),
                                         dimension.width,
                                         dimension.height))
                .thenReturn(thumbnail)

        val result = instance.getThumbnail(mediaId, dimension)
        assertThat(result, equalTo(thumbnail))
    }

    @DontRepeat
    @Test
    fun testGetThumbnailWithBadArgs()
    {
        assertThrows { instance.getThumbnail("", dimension) }.invalidArg()
        assertThrows { instance.getThumbnail(badId, dimension) }.invalidArg()
        assertThrows { instance.getThumbnail(mediaId, badDimension) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testGetThumbnailWhenFails()
    {
        setupForFailure()
        assertThrows { instance.getThumbnail(mediaId, dimension) }.operationError()
    }

    @DontRepeat
    @Test
    fun testGetThumbnailWhenDoesNotExist()
    {
        val sql = Queries.SELECT_MEDIA_THUMBNAIL

        whenever(database.queryForObject(sql,
                                         serializer,
                                         mediaId.toUUID(),
                                         dimension.width,
                                         dimension.height))
                .thenThrow(EmptyResultDataAccessException(0))

        assertThrows { instance.getThumbnail(mediaId, dimension) }.doesNotExist()
    }

    @Test
    fun testDeleteThumbnail()
    {
        val sql = Deletes.MEDIA_THUMBNAIL

        instance.deleteThumbnail(mediaId, dimension)

        verify(database).update(sql, mediaId.toUUID(), dimension.width, dimension.height)
    }

    @DontRepeat
    @Test
    fun testDeleteThumbnailWithBadArgs()
    {
        assertThrows { instance.deleteThumbnail("", dimension) }.invalidArg()
        assertThrows { instance.deleteThumbnail(badId, dimension) }.invalidArg()
        assertThrows { instance.deleteThumbnail(mediaId, badDimension) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteThumbnailWhenFails()
    {
        setupForFailure()
        assertThrows { instance.deleteThumbnail(mediaId, dimension) }.operationError()
    }

    @Test
    fun testDeleteAllThumbnails()
    {
        val sql = Deletes.ALL_MEDIA_THUMBNAILS

        instance.deleteAllThumbnails(mediaId)

        verify(database).update(sql, mediaId.toUUID())
    }

    @DontRepeat
    @Test
    fun testDeleteAllThumbnailsWithBadArgs()
    {
        assertThrows { instance.deleteAllThumbnails("") }.invalidArg()
        assertThrows { instance.deleteAllThumbnails(badId) }.invalidArg()
    }

    @DontRepeat
    @Test
    fun testDeleteAllThumbnailsWhenFails()
    {
        setupForFailure()
        assertThrows { instance.deleteAllThumbnails(mediaId) }.operationError()
    }

    private fun setupData()
    {
        image = Images.profileImage
        image.dimension = dimension
        thumbnail = Images.icon
        thumbnail.dimension = dimension

        badImage = Image()
        badDimension = Dimension(one(negativeIntegers()), one(negativeIntegers()))
    }

    private fun setupMocks()
    {

    }

    private fun setupForFailure()
    {
        whenever(database.queryForObject(any<String>(), eq(serializer), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())

        whenever(database.update(any<String>(), Mockito.anyVararg<Any>()))
                .thenThrow(RuntimeException())
    }

}