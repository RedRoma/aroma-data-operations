package tech.aroma.data.sql.serializers

/**
 * @author SirWellington
 */

import com.google.inject.Guice
import com.google.inject.Injector
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.aroma.data.hasInstance
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.thrift.*
import tech.aroma.thrift.authentication.AuthenticationToken
import tech.aroma.thrift.channels.MobileDevice
import tech.aroma.thrift.events.Event
import tech.aroma.thrift.reactions.Reaction
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import kotlin.test.assertTrue

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
    fun testHasAppSerializer()
    {
        assertTrue { injector.hasInstance<ApplicationSerializer>() }
    }

    @Test
    fun testHasDeviceSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<MutableSet<MobileDevice>>>() }
    }

    @Test
    fun testHasEventSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<Event>>() }
    }

    @Test
    fun testHasImageSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<Image>>() }
    }

    @Test
    fun testHasMessageSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<Message>>() }
    }

    @Test
    fun testHasOrganizationSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<Organization>>() }
    }

    @Test
    fun testHasReactionSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<MutableList<Reaction>>>() }
    }

    @Test
    fun testHasTokenSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<AuthenticationToken>>() }
    }

    @Test
    fun testHasUserSerializer()
    {
        assertTrue { injector.hasInstance<DatabaseSerializer<User>>() }
    }

}