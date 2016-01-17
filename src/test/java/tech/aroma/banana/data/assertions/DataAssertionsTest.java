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

package tech.aroma.banana.data.assertions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.Application;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class DataAssertionsTest 
{

    @GeneratePojo
    private Application application;
    
    @GenerateString
    private String string;
    
    @Before
    public void setUp()
    {
    }
    
    @DontRepeat
    @Test
    public void testConstuctor()
    {
        assertThrows(() -> new DataAssertions())
            .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testValidApplication()
    {
        AlchemyAssertion<Application> assertion = DataAssertions.validApplication();
        assertThat(assertion, notNullValue());
        
        assertion.check(application);
        
        assertThrows(() -> assertion.check(null))
            .isInstanceOf(FailedAssertionException.class);
        
        Application empty = new Application();
        assertThrows(() -> assertion.check(empty))
            .isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testIsNullOrEmpty()
    {
        assertThat(DataAssertions.isNullOrEmpty(string), is(false));
        assertThat(DataAssertions.isNullOrEmpty(""), is(true));
        assertThat(DataAssertions.isNullOrEmpty(null), is(true));
    }
    

}