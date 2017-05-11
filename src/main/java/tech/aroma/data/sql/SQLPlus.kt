package tech.aroma.data.sql

import tech.sirwellington.alchemy.arguments.Arguments.checkThat
import tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString
import java.sql.*
import java.util.*

/**
 *
 * @author SirWellington
 */
internal class SQLPlus
{

}


/**
 * Checks whether a [ResultSet] has a column present or not.
 */
public fun ResultSet.hasColumn(column: String): Boolean
{
    checkThat(column).`is`(nonEmptyString())

    try
    {
        this.findColumn(column)
        return true
    }
    catch (ex: SQLException)
    {
        return false
    }
}

public fun Long.toTimestamp(): Timestamp
{
    return Timestamp(this)
}

public fun String.asUUID(): UUID?
{
    return try
    {
        UUID.fromString(this)
    }
    catch (ex: Exception)
    {
        return null
    }
}
