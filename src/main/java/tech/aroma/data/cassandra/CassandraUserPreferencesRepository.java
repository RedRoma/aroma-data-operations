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

package tech.aroma.data.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.data.cassandra.Tables.UserPreferences;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.thrift.ThriftObjects;

import static com.datastax.driver.core.querybuilder.QueryBuilder.add;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.remove;
import static java.util.stream.Collectors.toSet;
import static tech.aroma.data.assertions.RequestAssertions.validMobileDevice;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
@StrategyPattern(role = CONCRETE_BEHAVIOR)
@ThreadSafe
final class CassandraUserPreferencesRepository implements UserPreferencesRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(CassandraUserPreferencesRepository.class);

    private final Session cassandra;
    private final Function<Row, Set<MobileDevice>> mobileDeviceMapper;

    @Inject
    CassandraUserPreferencesRepository(Session cassandra,
                                       Function<Row, Set<MobileDevice>> mobileDeviceMapper)
    {
        checkThat(cassandra, mobileDeviceMapper)
            .are(notNull());

        this.cassandra = cassandra;
        this.mobileDeviceMapper = mobileDeviceMapper;
    }

    @Override
    public void saveMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        checkUserId(userId);
        checkMobileDevice(mobileDevice);

        Statement statement = createStatementToAddDevice(userId, mobileDevice);
        tryToExecute(statement, "saveMobileDevice");
    }

    @Override
    public void saveMobileDevices(String userId, Set<MobileDevice> mobileDevices) throws TException
    {
        checkUserId(userId);
        checkMobileDevices(mobileDevices);

        Statement statement = createStatementToSaveDevice(userId, mobileDevices);

        tryToExecute(statement, "saveMobileDevices");
    }

    @Override
    public Set<MobileDevice> getMobileDevices(String userId) throws TException
    {
        checkUserId(userId);

        Statement query = createQueryToGetDevicesFor(userId);

        ResultSet results = tryToExecute(query, userId);

        Row row = results.one();
        if (row == null)
        {
            return Sets.emptySet();
        }

        return mobileDeviceMapper.apply(row);
    }

    @Override
    public void deleteMobileDevice(String userId, MobileDevice mobileDevice) throws TException
    {
        checkUserId(userId);
        checkMobileDevice(mobileDevice);

        Statement statement = createStatementToRemoveDevice(userId, mobileDevice);

        tryToExecute(statement, "deleteMobileDevice");
    }

    @Override
    public void deleteAllMobileDevices(String userId) throws TException
    {
        checkUserId(userId);

        Statement statement = createStatementToDeleteAllDevicesFor(userId);

        tryToExecute(statement, "deleteAllMobileDevices");
    }

    private Statement createStatementToSaveDevice(String userId, Set<MobileDevice> mobileDevices)
    {
        UUID userUuid = UUID.fromString(userId);

        Set<String> serializedDevices = Sets.nullToEmpty(mobileDevices)
            .stream()
            .map(this::serializeMobileDevice)
            .filter(Objects::nonNull)
            .collect(toSet());

        return QueryBuilder
            .insertInto(Tables.UserPreferences.TABLE_NAME)
            .value(Tables.UserPreferences.USER_ID, userUuid)
            .value(Tables.UserPreferences.SERIALIZED_DEVICES, serializedDevices);
    }

    private ResultSet tryToExecute(Statement statement, String operationName) throws OperationFailedException
    {
        try
        {
            return cassandra.execute(statement);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to execute operation {} on Cassandra.", operationName, ex);
            throw new OperationFailedException("Cassandra Operation Failed: " + ex.getMessage());
        }
    }

    private Statement createQueryToGetDevicesFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .select()
            .all()
            .from(UserPreferences.TABLE_NAME)
            .where(eq(UserPreferences.USER_ID, userUuid));
    }

    private Statement createStatementToDeleteAllDevicesFor(String userId)
    {
        UUID userUuid = UUID.fromString(userId);

        return QueryBuilder
            .delete()
            .all()
            .from(UserPreferences.TABLE_NAME)
            .where(eq(UserPreferences.USER_ID, userUuid));
    }

    private Statement createStatementToAddDevice(String userId, MobileDevice mobileDevice)
    {
        UUID userUuid = UUID.fromString(userId);
        String serializedDevice = serializeMobileDevice(mobileDevice);

        return QueryBuilder
            .update(UserPreferences.TABLE_NAME)
            .with(add(UserPreferences.SERIALIZED_DEVICES, serializedDevice))
            .where(eq(UserPreferences.USER_ID, userUuid));
    }

    private Statement createStatementToRemoveDevice(String userId, MobileDevice mobileDevice)
    {
        UUID userUuid = UUID.fromString(userId);
        String serializeDevice = serializeMobileDevice(mobileDevice);

        return QueryBuilder
            .update(UserPreferences.TABLE_NAME)
            .with(remove(UserPreferences.SERIALIZED_DEVICES, serializeDevice))
            .where(eq(UserPreferences.USER_ID, userUuid));

    }

    private String serializeMobileDevice(MobileDevice device)
    {
        if (device == null)
        {
            return null;
        }

        try
        {
            return ThriftObjects.toJson(device);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to Serialize Mobile Device {}", device, ex);
            return null;
        }
    }

    private void checkMobileDevice(MobileDevice mobileDevice) throws InvalidArgumentException
    {
        checkThat(mobileDevice)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validMobileDevice());
    }

    private void checkUserId(String userId) throws InvalidArgumentException
    {
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .is(validUserId());
    }

    private void checkMobileDevices(Set<MobileDevice> mobileDevices) throws InvalidArgumentException
    {
        checkThat(mobileDevices)
            .usingMessage("Mobile Devices cannot be null")
            .throwing(InvalidArgumentException.class)
            .is(notNull());

        for (MobileDevice device : mobileDevices)
        {
            checkMobileDevice(device);
        }
    }

}
