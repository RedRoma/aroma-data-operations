package tech.aroma.data.sql.serializers;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.data.sql.DatabaseSerializer;
import tech.aroma.thrift.Message;
import tech.sirwellington.alchemy.arguments.assertions.Assertions;
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions;

import static tech.aroma.data.assertions.RequestAssertions.validMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Serializes {@linkplain Message Messages} to the Database.
 *
 * @author SirWellington
 */
public class MessageSerializer implements DatabaseSerializer<Message>
{

    private static final Logger LOG = LoggerFactory.getLogger(MessageSerializer.class);

    @Override
    public void save(Message message, Duration timeToLive, String statement, JdbcTemplate database) throws SQLException
    {
        checkThat(message)
                .is(validMessage());

        checkThat(database)
                .is(notNull());

        checkThat(statement)
                .is(nonEmptyString());

        Timestamp expiration = null;

        if (Objects.nonNull(timeToLive))
        {
            Instant instanceOfExpiration = Instant.now().plus(timeToLive);
            expiration = Timestamp.from(instanceOfExpiration);
        }

        UUID appId = UUID.fromString(message.applicationId);
        UUID messageId = UUID.fromString(message.messageId);

        database.update(statement,
                        messageId,
                        message.title,
                        message.body,
                        message.urgency.toString(),
                        Timestamp.from(Instant.ofEpochMilli(message.timeOfCreation)),
                        Timestamp.from(Instant.ofEpochMilli(message.timeMessageReceived)),
                        expiration,
                        message.hostname,
                        message.macAddress,
                        appId,
                        message.applicationName,
                        message.deviceName);
    }
}
