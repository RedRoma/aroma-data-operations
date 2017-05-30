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
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Images
import tech.aroma.data.sql.SQLStatements.*
import tech.aroma.thrift.Dimension
import tech.aroma.thrift.Image
import tech.sirwellington.alchemy.test.junit.runners.*
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

    @Test
    fun testGetMedia()
    {
        val sql = Queries.SELECT_MEDIA

        whenever(database.queryForObject(sql, serializer, mediaId.toUUID()))
                .thenReturn(image)

        val result = instance.getMedia(mediaId)
        assertThat(result, equalTo(image))
    }

    @Test
    fun testDeleteMedia()
    {
        val sql = Deletes.MEDIA
        instance.deleteMedia(mediaId)

        verify(database).update(sql, mediaId.toUUID())
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

    @Test
    fun testDeleteThumbnail()
    {
        val sql = Deletes.MEDIA_THUMBNAIL

        instance.deleteThumbnail(mediaId, dimension)

        verify(database).update(sql, mediaId.toUUID(), dimension.width, dimension.height)
    }

    @Test
    fun testDeleteAllThumbnails()
    {
        val sql = Deletes.ALL_MEDIA_THUMBNAILS

        instance.deleteAllThumbnails(mediaId)

        verify(database).update(sql, mediaId.toUUID())
    }

    private fun setupData()
    {
        image = Images.profileImage
        image.dimension = dimension
        thumbnail = Images.icon
        thumbnail.dimension = dimension
    }

    private fun setupMocks()
    {

    }

}