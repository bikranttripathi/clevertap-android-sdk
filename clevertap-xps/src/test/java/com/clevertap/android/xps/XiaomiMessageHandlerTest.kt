package com.clevertap.android.xps

import com.clevertap.android.shared.test.BaseTestCase
import com.clevertap.android.shared.test.TestApplication
import com.google.gson.GsonBuilder
import com.xiaomi.mipush.sdk.ErrorCode
import com.xiaomi.mipush.sdk.MiPushClient
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import org.junit.*
import org.junit.runner.*
import org.mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = TestApplication::class)
class XiaomiMessageHandlerTest : BaseTestCase() {

    private lateinit var handler: XiaomiMessageHandler

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        handler = XiaomiMessageHandler()
    }

    @Test
    fun testCreateNotification_Null_Message() {
        val isSuccess = handler.createNotification(application, null)
        Assert.assertFalse(isSuccess)
    }

    @Test
    fun testCreateNotification_Invalid_Message() {
        val isSuccess = handler.createNotification(application, MiPushMessage())
        Assert.assertFalse(isSuccess)
    }

    @Test
    fun testCreateNotification_Valid_Message() {
        val message = Mockito.mock(MiPushMessage::class.java)
        Mockito.`when`(message.content).thenReturn(getMockJsonString())
        val isSuccess = handler.createNotification(application, message)
        Assert.assertTrue(isSuccess)
    }

    private fun getMockJsonString(): String? {
        val hashMap = HashMap<String, String>()
        hashMap.put("Title", "Sample Title")
        hashMap.put("Message", "Sample Message Title")
        return GsonBuilder().create().toJson(hashMap)
    }

    @Test
    fun testOnReceivePassThroughMessage_Other_Command() {
        val message = Mockito.mock(MiPushCommandMessage::class.java)
        Mockito.`when`(message.command).thenReturn(MiPushClient.COMMAND_UNSET_ACCOUNT)
        val result = handler.onReceiveRegisterResult(application, message)
        Assert.assertEquals(result, XpsConstants.OTHER_COMMAND)
    }

    @Test
    fun testOnReceivePassThroughMessage_Token_Success() {
        val message = Mockito.mock(MiPushCommandMessage::class.java)
        Mockito.`when`(message.command).thenReturn(MiPushClient.COMMAND_REGISTER)
        Mockito.`when`(message.resultCode).thenReturn(ErrorCode.SUCCESS.toLong())
        Mockito.`when`(message.commandArguments).thenReturn(listOf(XpsTestConstants.MI_TOKEN))
        val result = handler.onReceiveRegisterResult(application, message)
        Assert.assertEquals(result, XpsConstants.TOKEN_SUCCESS)
    }

    @Test
    fun testOnReceivePassThroughMessage_Invalid_Token() {
        val message = Mockito.mock(MiPushCommandMessage::class.java)
        Mockito.`when`(message.command).thenReturn(MiPushClient.COMMAND_REGISTER)
        Mockito.`when`(message.resultCode).thenReturn(ErrorCode.SUCCESS.toLong())
        Mockito.`when`(message.commandArguments).thenReturn(emptyList())
        val result = handler.onReceiveRegisterResult(application, message)
        Assert.assertEquals(result, XpsConstants.INVALID_TOKEN)
    }

    @Test
    fun testOnReceivePassThroughMessage_Token_Failure() {
        val message = Mockito.mock(MiPushCommandMessage::class.java)
        Mockito.`when`(message.command).thenReturn(MiPushClient.COMMAND_REGISTER)
        Mockito.`when`(message.resultCode).thenReturn(ErrorCode.ERROR_SERVICE_UNAVAILABLE.toLong())
        Mockito.`when`(message.commandArguments).thenReturn(emptyList())
        val result = handler.onReceiveRegisterResult(application, message)
        Assert.assertEquals(result, XpsConstants.TOKEN_FAILURE)
    }

    @Test
    fun testOnReceivePassThroughMessage_Failed_Exception() {
        val message = Mockito.mock(MiPushCommandMessage::class.java)
        Mockito.`when`(message.command).thenReturn(MiPushClient.COMMAND_REGISTER)
        Mockito.`when`(message.resultCode).thenReturn(ErrorCode.SUCCESS.toLong())
        Mockito.`when`(message.commandArguments).thenThrow(RuntimeException("Something went wrong"))
        val result = handler.onReceiveRegisterResult(application, message)
        Assert.assertEquals(result, XpsConstants.FAILED_WITH_EXCEPTION)
    }
}