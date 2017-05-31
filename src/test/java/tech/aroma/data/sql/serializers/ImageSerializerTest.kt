package tech.aroma.data.sql.serializers

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
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.AromaGenerators.Images
import tech.aroma.data.sql.serializers.Columns.Media
import tech.aroma.thrift.Dimension
import tech.aroma.thrift.Image
import tech.sirwellington.alchemy.test.junit.runners.*
import tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID
import java.sql.ResultSet

@RunWith(AlchemyTestRunner::class)
class ImageSerializerTest
{

    @Mock
    private lateinit var database: JdbcOperations

    @Mock
    private lateinit var row: ResultSet

    @GenerateString
    private lateinit var sql: String

    @GenerateString(UUID)
    private lateinit var imageId: String

    private lateinit var image: Image

    @GeneratePojo
    private lateinit var dimension: Dimension

    private lateinit var instance: ImageSerializer

    @Before
    fun setUp()
    {

        setupData()
        setupMocks()

        instance = ImageSerializer()
    }

    @Test
    fun testSave()
    {
        instance.save(image, sql, database)
    }

    @Test
    fun testDeserialize()
    {
        val result = instance.deserialize(row)
        val expected = Image(image).setDimension(dimension)

        assertThat(result, equalTo(expected))
    }

    private fun setupData()
    {
        image = Images.icon
    }

    private fun setupMocks()
    {
        whenever(row.getString(Media.MEDIA_TYPE)).thenReturn(image.imageType.toString())
        whenever(row.getInt(Media.WIDTH)).thenReturn(dimension.width)
        whenever(row.getInt(Media.HEIGHT)).thenReturn(dimension.height)
        whenever(row.getBytes(Media.DATA)).thenReturn(image.getData())
    }

}