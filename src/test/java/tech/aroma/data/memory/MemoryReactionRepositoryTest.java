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

package tech.aroma.data.memory;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.reactions.Reaction;
import tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class MemoryReactionRepositoryTest
{
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private List<Reaction> reactions;
    
    private MemoryReactionRepository instance;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new MemoryReactionRepository();
        
        setupData();
    }
    
    private void setupData() throws Exception
    {
        reactions = listOf(reactions(), 10);
    }
    
    @Test
    public void testSaveReactionsForUser() throws Exception
    {
        instance.saveReactionsForUser(userId, reactions);
    }
    
    @Test
    public void testSaveReactionsForUserWithEmpty() throws Exception
    {
        instance.saveReactionsForUser(userId, null);
        
        List<Reaction> result = instance.getReactionsForUser(userId);
        
        checkThat(result)
            .is(notNull())
            .is(CollectionAssertions.emptyList());
    }
    
    @Test
    public void testGetReactionsForUser() throws Exception
    {
        instance.saveReactionsForUser(userId, reactions);
        
        List<Reaction> result = instance.getReactionsForUser(userId);
        assertThat(result, is(reactions));
    }
    
    @Test
    public void testSaveReactionsForUserWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveReactionsForUser(badId, reactions))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testGetReactionsForUserWhenEmpty() throws Exception
    {
        List<Reaction> result = instance.getReactionsForUser(userId);
        assertThat(result, notNullValue());
        assertThat(result, empty());
    }
    
    @Test
    public void testGetReactionsForUserWithBadArgs() throws Exception
    {
        
        assertThrows(() -> instance.getReactionsForUser(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testSaveReactionsForApplication() throws Exception
    {
        instance.saveReactionsForApplication(appId, reactions);
    }
    
    @Test
    public void testSaveReactionsForApplicationWithEmpty() throws Exception
    {
        instance.saveReactionsForApplication(appId, null);
    }
    
    @Test
    public void testSaveReactionsForApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.saveReactionsForApplication(badId, reactions))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testGetReactionsForApplication() throws Exception
    {
        instance.saveReactionsForApplication(appId, reactions);
        
        List<Reaction> result = instance.getReactionsForApplication(appId);
        assertThat(result, is(reactions));
    }
    
    @Test
    public void testGetReactionsForApplicationWhenEmpty() throws Exception
    {
        List<Reaction> result = instance.getReactionsForApplication(appId);
        assertThat(result, notNullValue());
        assertThat(result, empty());
    }
    
    @Test
    public void testGetReactionsForApplicationWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getReactionsForApplication(badId))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
}
