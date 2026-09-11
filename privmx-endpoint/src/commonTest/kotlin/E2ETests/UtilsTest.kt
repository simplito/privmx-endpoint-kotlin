package E2ETests

import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import com.simplito.kotlin.privmx_endpoint.modules.core.utils.Hex
import com.simplito.kotlin.privmx_endpoint.modules.core.utils.Base32
import com.simplito.kotlin.privmx_endpoint.modules.core.utils.Base64
import com.simplito.kotlin.privmx_endpoint.modules.core.utils.Utils
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilsTest {
    @Test
    @Throws(PrivmxException::class)
    fun hexTest() {
        val data = "Test Hex".encodeToByteArray()
        val dataInHex = "5465737420486578"

        assertEquals(dataInHex, Hex.encode(data))
        assertContentEquals(data, Hex.decode(dataInHex))
        assertTrue(Hex.`is`(dataInHex))
        assertFalse(Hex.`is`(data.decodeToString()))
    }

    @Test
    @Throws(PrivmxException::class)
    fun base32() {
        val data = "Test Base32".encodeToByteArray()
        val dataInBase32 = "KRSXG5BAIJQXGZJTGI======"

        assertEquals(dataInBase32, Base32.encode(data))
        assertContentEquals(data, Base32.decode(dataInBase32))
        assertTrue(Base32.`is`(dataInBase32))
        assertFalse(Base32.`is`(data.decodeToString()))
    }

    @Test
    @Throws(PrivmxException::class)
    fun base64() {
        val data = "Test Base64".encodeToByteArray()
        val dataInBase64 = "VGVzdCBCYXNlNjQ="

        assertEquals(dataInBase64, Base64.encode(data))
        assertContentEquals(data, Base64.decode(dataInBase64))
        assertTrue(Base64.`is`(dataInBase64))
        assertFalse(Base64.`is`(data.decodeToString()))
    }

    @Test
    @Throws(PrivmxException::class)
    fun utils() {
        val data = "  U t i l s t e s t  "

        assertEquals("U t i l s t e s t", Utils.trim(data))
        assertEquals("U t i l s t e s t  ", Utils.ltrim(data))
        assertEquals("  U t i l s t e s t", Utils.rtrim(data))

        var splitList: List<String?> = Utils.split(data, " ")
        assertEquals(13, splitList.size)

        splitList = Utils.split(data, "  ")
        assertEquals(3, splitList.size)

        splitList = Utils.split(Utils.trim(data), "  ")
        assertEquals(1, splitList.size)
    }
}