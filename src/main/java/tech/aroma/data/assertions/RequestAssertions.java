package tech.aroma.data.assertions;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.thrift.*;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.channels.*;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptySet;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveLong;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * @author SirWellington
 */
@NonInstantiable
@Internal
public final class RequestAssertions
{
    private final static Logger LOG = LoggerFactory.getLogger(RequestAssertions.class);

    RequestAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether an {@link Application} is valid or not.
     */
    public static AlchemyAssertion<Application> validApplication()
    {
        return app ->
        {
            checkThat(app)
                    .usingMessage("app is missing")
                    .is(notNull());

            checkThat(app.applicationId, app.name)
                    .are(nonEmptyString());

            checkThat(app.applicationId)
                    .is(validApplicationId());

            checkThat(app.owners)
                    .usingMessage("app is missing owners")
                    .is(nonEmptySet());

            app.owners.forEach(owner -> checkThat(owner)
                                            .usingMessage("Owner ID must be a valid user ID: " + owner)
                                            .is(validUserId()));

            if (app.isSetOrganizationId())
            {
                checkThat(app.organizationId)
                        .is(validOrgId());
            }
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a {@link Message} is valid.
     */
    public static AlchemyAssertion<Message> validMessage()
    {
        return message ->
        {
            checkThat(message)
                    .usingMessage("message is missing")
                    .is(notNull());

            checkThat(message.messageId)
                    .is(validMessageId());

            checkThat(message.title)
                    .usingMessage("message missing Title")
                    .is(nonEmptyString());

            if (message.isSetApplicationId())
            {
                checkThat(message.applicationId)
                        .is(validApplicationId());
            }
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a {@link Organization} is valid.
     */
    public static AlchemyAssertion<Organization> validOrganization()
    {
        return org ->
        {
            checkThat(org)
                    .usingMessage("org is missing")
                    .is(notNull());

            checkThat(org.organizationId)
                    .is(validOrgId());

            checkThat(org.organizationName)
                    .usingMessage("missing organization name")
                    .is(nonEmptyString());

            List<String> owners = Lists.nullToEmpty(org.owners);
            for (String owner : owners)
            {
                checkThat(owner)
                        .is(validUserId());
            }
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a {@link User} is valid.
     */
    public static AlchemyAssertion<User> validUser()
    {
        return user ->
        {
            checkThat(user)
                    .usingMessage("user is missing")
                    .is(notNull());

            checkThat(user.userId)
                    .is(validUserId());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a String is a valid App ID.
     */
    public static AlchemyAssertion<String> validApplicationId()
    {
        return appId ->
        {
            checkThat(appId)
                    .usingMessage("missing appId")
                    .is(nonEmptyString())
                    .usingMessage("appId must be a UUID")
                    .is(validUUID());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether an
     * {@link AndroidDevice} is valid.
     */
    public static AlchemyAssertion<AndroidDevice> validAndroidDevice()
    {
        return android ->
        {
            checkThat(android)
                    .usingMessage("Android Device cannot be null")
                    .is(notNull());

            checkThat(android.registrationId)
                    .usingMessage("Android Registration ID cannot be empty")
                    .is(nonEmptyString());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether an {@link IOSDevice} is valid.
     */
    public static AlchemyAssertion<IOSDevice> validiOSDevice()
    {
        return ios ->
        {
            checkThat(ios)
                    .usingMessage("iOS Device cannot be null")
                    .is(notNull());

            checkThat(ios.deviceToken)
                    .usingMessage("iOS Device Token cannot be empty")
                    .is(notNull());

            checkThat(ios.getDeviceToken().length)
                    .usingMessage("iOS Device Token cannot be empty")
                    .is(greaterThan(0));
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a {@link MobileDevice} is valid.
     */
    public static AlchemyAssertion<MobileDevice> validMobileDevice()
    {
        return device ->
        {
            checkThat(device)
                    .usingMessage("Mobile Device cannot be null")
                    .is(notNull());

            checkThat(device.isSet())
                    .usingMessage("Mobile Device must be set")
                    .is(trueStatement());

            if (device.isSetAndroidDevice())
            {
                checkThat(device.getAndroidDevice())
                        .is(validAndroidDevice());
            }

            if (device.isSetIosDevice())
            {
                checkThat(device.getIosDevice())
                        .is(validiOSDevice());
            }

        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a String is a valid
     * {@linkplain Message#messageId Message ID}.
     */
    public static AlchemyAssertion<String> validMessageId()
    {
        return msgId ->
        {
            checkThat(msgId)
                    .usingMessage("missing messageID")
                    .is(nonEmptyString())
                    .usingMessage("messageID must be a UUID type")
                    .is(validUUID());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a String is a valid
     * {@linkplain Organization#organizationId Organization ID}.
     */
    public static AlchemyAssertion<String> validOrgId()
    {
        return orgId ->
        {
            checkThat(orgId)
                    .usingMessage("missing orgId")
                    .is(nonEmptyString())
                    .usingMessage("orgId must be a UUID")
                    .is(validUUID());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a String is a valid
     * {@linkplain User#userId UserID}.
     */
    public static AlchemyAssertion<String> validUserId()
    {
        return userId ->
        {
            checkThat(userId)
                    .usingMessage("missing userId")
                    .is(nonEmptyString())
                    .usingMessage("userId must be a UUID")
                    .is(validUUID());
        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether an {@link AuthenticationToken}
     * contains an Owner.
     */
    public static AlchemyAssertion<AuthenticationToken> tokenContainingOwnerId()
    {
        return token ->
        {
            checkThat(token)
                    .usingMessage("token is null")
                    .is(notNull());

            checkThat(token.ownerId)
                    .usingMessage("token missing ownerId")
                    .is(nonEmptyString());

        };
    }

    /**
     * @return An {@linkplain AlchemyAssertion Assertion} that checks whether a {@link LengthOfTime} is valid.
     */
    public static AlchemyAssertion<LengthOfTime> validLengthOfTime()
    {
        return time ->
        {
            notNull().check(time);

            checkThat(time.value)
                    .usingMessage("Time value must be positive")
                    .is(positiveLong());

            checkThat(time.unit)
                    .usingMessage("Time is missing unit")
                    .is(notNull());
        };
    }

    public static boolean isNullOrEmpty(@Optional String string)
    {
        return string == null || string.isEmpty();
    }


}
