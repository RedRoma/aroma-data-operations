/*
 * Copyright 2016 Aroma Tech.
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

 
package tech.aroma.banana.data.cassandra;


import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
class Times 
{
    private final static Logger LOG = LoggerFactory.getLogger(Times.class);

    
    static long toSeconds(@Required LengthOfTime lengthOfTime)
    {
        checkThat(lengthOfTime)
            .is(notNull());
        
        Duration duration = TimeFunctions.lengthOfTimeToDuration().apply(lengthOfTime);
        return duration.getSeconds();
    }
}
