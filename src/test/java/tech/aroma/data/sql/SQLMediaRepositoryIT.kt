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
import org.junit.*
import org.junit.runner.RunWith
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Images
import tech.aroma.data.doesNotExist
import tech.aroma.data.operationError
import tech.aroma.data.sql.serializers.ImageSerializer
import tech.aroma.thrift.Dimension
import tech.aroma.thrift.Image
import tech.aroma.thrift.exceptions.DoesNotExistException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AlchemyTestRunner::class)
class SQLMediaRepositoryIT
{

    private val serializer = ImageSerializer()

    private lateinit var image: Image
    private lateinit var thumbnail: Image

    @GeneratePojo
    private lateinit var dimension: Dimension

    @GenerateList(Dimension::class, size = 5)
    private lateinit var dimensions: List<Dimension>

    @GenerateString(UUID)
    private lateinit var mediaId: String

    private lateinit var instance: SQLMediaRepository

    @Before
    fun setUp()
    {

        setupData()
        instance = SQLMediaRepository(database, serializer)
    }

    @After
    fun destroy()
    {
        try
        {
            instance.deleteMedia(mediaId)
        }
        catch (ex: Exception)
        {
            println(ex)
        }

        try
        {
            instance.deleteAllThumbnails(mediaId)
        }
        catch (ex: Exception)
        {
            println(ex)
        }
    }

    @Test
    fun testSaveMedia()
    {
        instance.saveMedia(mediaId, image)

        val exists = checkExists(mediaId)
        assertTrue { exists }
    }


    @Test
    fun testSaveMediaTwice()
    {
        instance.saveMedia(mediaId, image)

        assertThrows {
            instance.saveMedia(mediaId, image)
        }.operationError()
    }


    @Test
    fun testGetMedia()
    {
        instance.saveMedia(mediaId, image)

        val result = instance.getMedia(mediaId)

        assertThat(result, equalTo(image))
    }

    @Test
    fun testGetMediaWhenDoesNotExist()
    {
        assertThrows { instance.getMedia(mediaId) }.doesNotExist()
    }

    @Test
    fun testDeleteMedia()
    {
        instance.saveMedia(mediaId, image)

        instance.deleteMedia(mediaId)

        val exists = checkExists(mediaId)
        assertFalse { exists }
    }

    @Test
    fun testDeleteMediaWhenNone()
    {
        instance.deleteMedia(mediaId)
    }

    @Test
    fun testSaveThumbnail()
    {
        instance.saveThumbnail(mediaId, dimension, thumbnail)

        val result = instance.getThumbnail(mediaId, dimension)

        val expected = Image(thumbnail).setDimension(dimension)
        assertThat(result, equalTo(expected))
    }

    @Test
    fun testSaveThumbnailTwice()
    {
        instance.saveThumbnail(mediaId, dimension, thumbnail)

        assertThrows {
            instance.saveThumbnail(mediaId, dimension, thumbnail)
        }.operationError()
    }

    @Test
    fun testGetThumbnail()
    {
        instance.saveThumbnail(mediaId, dimension, thumbnail)

        val result = instance.getThumbnail(mediaId, dimension)
        val expected = Image(thumbnail).setDimension(dimension)

        assertThat(result, equalTo(expected))
    }

    @Test
    fun testGetThumbnailWithMultiple()
    {
        dimensions.forEach { instance.saveThumbnail(mediaId, it, thumbnail) }

        dimensions.forEach {
            val result = instance.getThumbnail(mediaId, it)
            val expected = Image(thumbnail).setDimension(it)
            assertThat(result, equalTo(expected))
        }
    }

    @Test
    fun testGetThumbnailWhenDoesNotExist()
    {
        assertThrows { instance.getThumbnail(mediaId, dimension) }
                .doesNotExist()
    }

    @Test
    fun testDeleteThumbnail()
    {
        instance.saveThumbnail(mediaId, dimension, thumbnail)

        instance.deleteThumbnail(mediaId, dimension)

        assertThrows { instance.getThumbnail(mediaId, dimension) }
                .doesNotExist()
    }

    @Test
    fun testDeleteThumbnailWhenDoesNotExist()
    {
        instance.deleteThumbnail(mediaId, dimension)
    }

    @Test
    fun testDeleteAllThumbnails()
    {
        dimensions.forEach { instance.saveThumbnail(mediaId, it, thumbnail) }

        instance.deleteAllThumbnails(mediaId)

        dimensions.forEach {
            assertThrows { instance.getThumbnail(mediaId, it) }.doesNotExist()
        }
    }

    @Test
    fun testDeleteAllThumbnailsWhenNone()
    {
        instance.deleteAllThumbnails(mediaId)
    }

    private fun setupData()
    {
        image = Images.profileImage
        thumbnail = Images.icon
    }


    private fun checkExists(mediaId: String): Boolean
    {

        val query = "SELECT count(media_id) FROM Media WHERE media_id = ?"
        val exists = database.queryForObject(query, Boolean::class.java, mediaId.toUUID())

        return exists
    }

    companion object
    {
        @JvmStatic private lateinit var database: JdbcOperations

        @JvmStatic
        @BeforeClass
        fun setupDatabase()
        {
            database = TestingResources.connectToDatabase()
        }
    }
}