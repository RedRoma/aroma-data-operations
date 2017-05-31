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

package tech.aroma.data.sql.serializers

import org.springframework.jdbc.core.JdbcOperations
import tech.aroma.data.assertions.RequestAssertions.validImage
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.sql.serializers.Columns.Media
import tech.aroma.thrift.*
import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.ResultSet


/**
 *
 * @author SirWellington
 */
internal class ImageSerializer : DatabaseSerializer<Image>
{
    override fun save(image: Image, statement: String, database: JdbcOperations)
    {
        checkThat(statement).`is`(nonEmptyString())
        checkThat(image).`is`(validImage())
    }

    override fun deserialize(row: ResultSet): Image
    {
        val image = Image()

        val mediaType = row.getString(Media.MEDIA_TYPE)
        val width = row.getInt(Media.WIDTH)
        val height = row.getInt(Media.HEIGHT)
        val data = row.getBytes(Media.DATA)

        if (width > 0 && height > 0)
        {
            image.dimension = Dimension(width, height)
        }

        image.imageType = mediaType.toImageType()
        image.setData(data)

        return image
    }


    private fun String.toImageType(): ImageType?
    {
        return try
        {
            ImageType.valueOf(this)
        }
        catch(ex: Exception)
        {
            return null
        }
    }
}