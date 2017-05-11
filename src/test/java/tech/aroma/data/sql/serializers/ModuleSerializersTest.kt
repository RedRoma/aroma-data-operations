package tech.aroma.data.sql.serializers

/**
 * @author SirWellington
 */

import com.google.inject.*
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.thrift.Message
import tech.aroma.thrift.Organization
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner

@RunWith(AlchemyTestRunner::class)
class ModuleSerializersTest
{

    private lateinit var instance: ModuleSerializers
    private lateinit var injector: Injector

    @Before
    fun setup()
    {
        instance = ModuleSerializers()

        injector = Guice.createInjector(instance)
    }

    @Test
    fun testHasMessageSerializer()
    {
        val literal = object: TypeLiteral<DatabaseSerializer<Message>>() {}

        val result = injector.getInstance(Key.get(literal))
        assertThat(result, notNullValue())
    }

    @Test
    fun testHasOrganizationSerializer()
    {
        val literal = object: TypeLiteral<DatabaseSerializer<Organization>>() {}

        val result = injector.getInstance(Key.get(literal))
        assertThat(result, notNullValue())
    }
}