package tech.aroma.data.sql

/**
 * @author SirWellington
 */

import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.arguments.FailedAssertionException
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat
import tech.sirwellington.alchemy.test.junit.runners.GenerateString
import java.sql.ResultSet
import java.sql.SQLException

@RunWith(AlchemyTestRunner::class)
class SQLAssertionsTest
{

    @GenerateString
    private lateinit var column: String

    @Mock
    private lateinit var results: ResultSet

    @Before
    fun setup()
    {

    }

    @Test
    fun testResultSetWithColumn()
    {
        whenever(results.findColumn(column)).thenReturn(0)

        val assertion = SQLAssertions.resultSetWithColumn(column)

        assertion.check(results)
    }

    @Test
    fun testResultSetWithColumnWhenDoesNotHaveColumn()
    {
        whenever(results.findColumn(column))
                .thenThrow(SQLException())

        val assertion = SQLAssertions.resultSetWithColumn(column)

        assertThrows {
            assertion.check(results)
        }.isInstanceOf(FailedAssertionException::class.java)
    }

    @DontRepeat
    @Test
    fun testResultSetWithColumnWithBadArgs()
    {
        assertThrows {
            SQLAssertions.resultSetWithColumn("")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}