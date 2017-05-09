package tech.aroma.data.sql.serializers

import com.google.inject.*
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import tech.aroma.data.sql.DatabaseSerializer
import tech.aroma.thrift.Message

/**
 *
 * @author SirWellington
 */
class ModuleSerializers : AbstractModule()
{
    override fun configure()
    {
        bind<DatabaseSerializer<Message>>().to<MessageSerializer>().asEagerSingleton()
    }

    private inline fun <reified T : Any> AbstractModule.bind(): AnnotatedBindingBuilder<T> = binder().bind<T>()

}

inline fun <reified T : Any> Binder.bind(): AnnotatedBindingBuilder<T>
{
    val literal = object : TypeLiteral<T>() {}
    return bind(literal)
}

inline fun <reified T : Any> AnnotatedBindingBuilder<in T>.to(): ScopedBindingBuilder = to(T::class.java)

inline fun <reified T : Any> Injector.getInstance() = getInstance(T::class.java)