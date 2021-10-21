package proxyauth

import proxyauth.conf.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.Socket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestProxyRequest {
    private val mockProxyRequest = ProxyRequest(Socket(), ProxyListener(Configuration()), ThreadGroup("AMONGUS"))

    @Test
    fun `processHeaders should work`() {
        assertEquals(
            listOf("test", "data"),
            mockProxyRequest.processHeaders(ByteArrayInputStream("test\r\ndata\r\n\r\n".toByteArray())),
        )
    }

    @Test
    fun `processHeaders should stop at the body`() {
        val inputStream = ByteArrayInputStream("test\r\ndata\r\n\r\nb".toByteArray())
        mockProxyRequest.processHeaders(inputStream)
        // check that the body is next in the buffer
        assertEquals('b'.code.toByte(), inputStream.read().toByte())
    }

    @Test
    fun `processHeaders should fail when over buffer length`() {
        assertEquals("Buffer full before http request headers read", assertFailsWith<IOException> {
            mockProxyRequest.processHeaders(
                ByteArrayInputStream(
                    ByteArray(mockProxyRequest.parent.config.BUF_SIZE.value + 1)
                )
            )
        }.message)
    }

    @Test
    fun `processHeaders should fail when EOF reached without headers`() {
        assertEquals("End of stream reached before http request headers read", assertFailsWith<IOException> {
            mockProxyRequest.processHeaders(ByteArrayInputStream(ByteArray(0)))
        }.message)
        assertEquals("End of stream reached before http request headers read", assertFailsWith<IOException> {
            mockProxyRequest.processHeaders(ByteArrayInputStream("asdffdsa".toByteArray()))
        }.message)
    }
}
