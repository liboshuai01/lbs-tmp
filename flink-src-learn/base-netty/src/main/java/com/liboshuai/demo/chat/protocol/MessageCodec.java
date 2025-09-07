package com.liboshuai.demo.chat.protocol;

import com.liboshuai.demo.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    public static final MessageCodec INSTANCE = new MessageCodec();


    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        // 1. 4字节的魔数
        buffer.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节的版本
        buffer.writeByte(1);
        // 3. 1字节的序列化方式（jdk-0，json-1）
        buffer.writeByte(0);
        // 4. 1字节的指令类型
        buffer.writeByte(msg.getMessageType());
        // 5. 4字节的消息序号（int）
        buffer.writeInt(msg.getSequenceId());
        // 6. 1字节为了对其2的倍数填充字节
        buffer.writeByte(0xff);
        // 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 7. 4字节的信息长度（int）
        buffer.writeInt(bytes.length);
        // 8. 剩下都为消息内容
        buffer.writeBytes(bytes);
        out.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        // 1. 4字节的魔数
        byte[] magicBytes = new byte[4];
        msg.readBytes(magicBytes);
        // 2. 1字节的版本
        byte version = msg.readByte();
        // 3. 1字节的序列化方式（jdk-0，json-1）
        byte serializerType = msg.readByte();
        // 4. 1字节的指令类型
        byte messageType = msg.readByte();
        // 5. 4字节的消息序号（int）
        int sequenceId = msg.readInt();
        // 6. 1字节为了对其2的倍数填充字节
        msg.readByte();
        // 获取内容的字节数组
        // 7. 4字节的信息长度（int）
        int length = msg.readInt();
        // 8. 剩下都为消息内容
        byte[] messageBytes = new byte[length];
        msg.readBytes(messageBytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(messageBytes));
        Message message = (Message) ois.readObject();
        out.add(message);
        log.debug("magicNum: {}, version: {}, serializerType: {}, messageType: {}, sequenceId: {}, length: {}",
                magicBytes, version, serializerType, messageType, sequenceId, length);
        log.debug("message: {}", message);
    }
}
