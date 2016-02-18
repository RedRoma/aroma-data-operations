package tech.aroma.banana.data.assertions;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Organization;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
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
            
            if(app.isSetOrganizationId())
            {
                checkThat(app.organizationId)
                    .is(validOrgId());
            }
        };
    }
    
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

    public static boolean isNullOrEmpty(@Optional String string)
    {
        return string == null || string.isEmpty();
    }
    
    
}
