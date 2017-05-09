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


package tech.aroma.data.sql;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.data.sql.serializers.ModuleSerializers;
import tech.aroma.thrift.Message;

/**
 * @author SirWellington
 */

public class Resources
{

    public static Logger LOG = LoggerFactory.getLogger(Resources.class);

    private static final Injector injector = Guice.createInjector(new ModuleTesting(),
                                                                  new ModuleSerializers());


    public static JdbcTemplate connectToDatabase()
    {
        return injector.getInstance(JdbcTemplate.class);
    }

    public static DatabaseSerializer<Message> getMessageSerializer()
    {
        TypeLiteral<DatabaseSerializer<Message>> literal = new TypeLiteral<DatabaseSerializer<Message>>()
        {
        };
        return injector.getInstance(Key.get(literal));
    }
}
