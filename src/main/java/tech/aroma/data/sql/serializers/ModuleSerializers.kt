package tech.aroma.data.sql.serializers

import com.google.inject.AbstractModule
import com.google.inject.binder.AnnotatedBindingBuilder
import tech.aroma.data.bind
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.data.to
import tech.aroma.thrift.*
import tech.aroma.thrift.authentication.AuthenticationToken

/**
 *
 * @author SirWellington
 */
class ModuleSerializers : AbstractModule()
{
    override fun configure()
    {
        bind<DatabaseSerializer<Application>>().to<ApplicationSerializer>().asEagerSingleton()
        bind<DatabaseSerializer<AuthenticationToken>>().to<TokenSerializer>().asEagerSingleton()
        bind<DatabaseSerializer<Message>>().to<MessageSerializer>().asEagerSingleton()
        bind<DatabaseSerializer<Organization>>().to<OrganizationSerializer>().asEagerSingleton()
        bind<DatabaseSerializer<User>>().to<UserSerializer>().asEagerSingleton()
    }

    private inline fun <reified T : Any> AbstractModule.bind(): AnnotatedBindingBuilder<T> = binder().bind<T>()

}