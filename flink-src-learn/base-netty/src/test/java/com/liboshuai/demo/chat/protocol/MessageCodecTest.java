package com.liboshuai.demo.chat.protocol;

import com.liboshuai.demo.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

/**
 * MessageCodec的单元测试类
 */
class MessageCodecTest {

    @Test
    void testEncodeAndDecode() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(),
                new MessageCodec()
        );

        // encode
        LoginRequestMessage message = new LoginRequestMessage("lbs", "YOUR_PASSWORD");
        embeddedChannel.writeOutbound(message);

        // decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
        embeddedChannel.writeInbound(buf);
    }
}

