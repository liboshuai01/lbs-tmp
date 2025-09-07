package com.liboshuai.demo.chat.protocol;

import com.liboshuai.demo.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

/**
 * MessageCodec的单元测试类
 */
class MessageCodecTest {

    @Test
    void testEncodeAndDecode() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new LoggingHandler(),
                new MessageCodec()
        );

        // encode
        LoginRequestMessage message = new LoginRequestMessage("lbs", "YOUR_PASSWORD");
        embeddedChannel.writeOutbound(message);

        System.out.println("=============================================================");

        // decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);
        ByteBuf buf1 = buf.slice(0, 100);
        buf1.retain();
        ByteBuf buf2 = buf.slice(100, buf.readableBytes() - 100);
        buf2.retain();
        embeddedChannel.writeInbound(buf1);
        embeddedChannel.writeInbound(buf2);
    }
}

