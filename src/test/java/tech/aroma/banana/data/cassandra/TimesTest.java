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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.functions.TimeFunctions;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.aroma.banana.thrift.TimeUnit.DAYS;
import static tech.aroma.banana.thrift.TimeUnit.MINUTES;
import static tech.aroma.banana.thrift.TimeUnit.SECONDS;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class TimesTest 
{
    
    private LengthOfTime lengthOfTime;

    private long expectedSeconds;
    
    @Before
    public void setUp()
    {
        List<TimeUnit> units = Lists.createFrom(SECONDS, MINUTES, DAYS);
        TimeUnit unit = Lists.oneOf(units);
        long value = one(integers(1, 100));
        
        lengthOfTime = new LengthOfTime(unit, value);
        
        Duration duration = TimeFunctions.lengthOfTimeToDuration().apply(lengthOfTime);
        expectedSeconds = duration.getSeconds();
    }

    @Test
    public void testToSeconds()
    {
        long seconds = Times.toSeconds(lengthOfTime);
        assertThat(seconds, is(expectedSeconds));
    }

}