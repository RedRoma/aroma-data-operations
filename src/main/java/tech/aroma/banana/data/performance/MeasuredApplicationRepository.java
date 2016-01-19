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

package tech.aroma.banana.data.performance;

import decorice.DecoratedBy;
import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.thrift.Application;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This class decorates an existing {@link ApplicationRepository} and measures the latencies of the operations performed, in ms.
 * It performs no validation of input and catches no exceptions.
 *
 * @author SirWellington
 */
@DecoratorPattern(role = DECORATOR)
final class MeasuredApplicationRepository implements ApplicationRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MeasuredApplicationRepository.class);

    private final ApplicationRepository delegate;

    @Inject
    MeasuredApplicationRepository(@DecoratedBy(MeasuredApplicationRepository.class) ApplicationRepository delegate)
    {
        checkThat(delegate).is(notNull());

        this.delegate = delegate;
    }

    @Override
    public void saveApplication(Application application) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            delegate.saveApplication(application);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("saveApplication Operation took {} ms", end - start);
        }
    }

    @Override
    public void deleteApplication(String applicationId) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            delegate.deleteApplication(applicationId);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("deleteApplication Operation took {} ms", end - start);
        }
    }

    @Override
    public Application getById(String applicationId) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.getById(applicationId);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("getById Operation took {} ms", end - start);
        }
    }

    @Override
    public boolean containsApplication(String applicationId) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.containsApplication(applicationId);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("containsApplication Operation took {} ms", end - start);
        }
    }

    @Override
    public List<Application> getApplicationsOwnedBy(String userId) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.getApplicationsOwnedBy(userId);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("getApplicationsOwnedBy Operation took {} ms", end - start);
        }
    }

    @Override
    public List<Application> getApplicationsByOrg(String orgId) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.getApplicationsByOrg(orgId);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("getApplicationsByOrg Operation took {} ms", end - start);
        }
    }

    @Override
    public List<Application> searchByName(String searchTerm) throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.searchByName(searchTerm);
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("searchByName Operation took {} ms", end - start);
        }
    }

    @Override
    public List<Application> getRecentlyCreated() throws TException
    {
        long start = System.currentTimeMillis();

        try
        {
            return delegate.getRecentlyCreated();
        }
        finally
        {
            long end = System.currentTimeMillis();
            LOG.debug("getRecentlyCreated Operation took {} ms", end - start);
        }
    }

}
