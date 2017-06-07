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

package tech.aroma.data.performance;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.data.performance.Operation.VoidOperation;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.longs;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(5)
@RunWith(AlchemyTestRunner.class)
public class OperationsTest 
{

    @GenerateInteger
    private int value;
    
    @GenerateString
    private String operationName;
    
    @Before
    public void setUp()
    {
    }

    @Test
    public void testMeasureOperation() throws Exception
    {
        long sleepTimeMillis = one(longs(5, 50));
        
        VoidOperation operation = () -> 
        {
            try
            {
                Thread.sleep(sleepTimeMillis);
            }
            catch (InterruptedException ex)
            {
                fail("Interrupted:" + ex);
            }
           
            System.out.println("Operation complete");
        };
        
        long result = Operations.measureOperation(operation);
        assertThat(result, greaterThanOrEqualTo(sleepTimeMillis));
    }
    
    @Test
    public void testMeasureOperationWhenOperationThrows() throws Exception
    {
        VoidOperation operation = () ->
        {
           throw new TException();
        };
        
        assertThrows(() -> Operations.measureOperation(operation))
            .isInstanceOf(TException.class);
        
    }

    @Test
    public void testLogLatency() throws Exception
    {
        Operation<Integer> operation = mock(Operation.class);
        when(operation.call()).thenReturn(value);
        
        Integer result = Operations.logLatency(operation, operationName);
        assertThat(result, is(value));
    }

}
