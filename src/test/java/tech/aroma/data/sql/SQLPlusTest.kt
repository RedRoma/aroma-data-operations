package tech.aroma.data.sql

/**
 * @author SirWellington
 */

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows
import tech.sirwellington.alchemy.test.junit.runners.*
import java.sql.ResultSet
import java.sql.SQLException

@RunWith(AlchemyTestRunner::class)
class SQLPlusTest
{
    @GenerateString
    private lateinit var column: String

    @Mock
    private lateinit var results: ResultSet


    @Test
    fun testHasColumnWhenHasColumn()
    {
        whenever(results.findColumn(column)).thenReturn(0)

        val result: Boolean = results.hasColumn(column)
        assertThat(result, equalTo(true))
    }

    @Test
    fun testHasColumnWhenDoesNotHaveColumn()
    {
        whenever(results.findColumn(column)).thenThrow(SQLException())

        val result = results.hasColumn(column)

        assertThat(result, equalTo(false))
    }

    @DontRepeat
    @Test
    fun testHasColumnWithBadArgs()
    {
        assertThrows {
            results.hasColumn("")
        }
    }
}