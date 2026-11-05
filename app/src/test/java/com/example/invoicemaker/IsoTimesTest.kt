package com.example.invoicemaker

import com.example.invoicemaker.network.IsoTimes
import org.junit.Assert.assertEquals
import org.junit.Test

class IsoTimesTest {

    @Test
    fun `round trip keeps the same second`() {
        val millis = 1_762_000_000_000L
        val parsed = IsoTimes.toMillis(IsoTimes.toIso(millis))
        assertEquals(millis, parsed)
    }

    @Test
    fun `supabase offset timestamp parses the clock time as utc`() {
        val parsed = IsoTimes.toMillis("2026-11-05T10:18:22.123456+00:00")
        val expected = IsoTimes.toMillis("2026-11-05T10:18:22Z")
        assertEquals(expected, parsed)
    }
}