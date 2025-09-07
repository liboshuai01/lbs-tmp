package com.liboshuai.demo.chat.protocol;

import com.liboshuai.demo.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    /**
     * 编码：将消息转为ByteBuf
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 1. 4字节的魔数
        out.writeBytes(new byte[]{1,2,3,4});
        // 2. 1字节的版本
        out.writeByte(1);
        // 3. 1字节的序列化方式（jdk-0，json-1）
        out.writeByte(0);
        // 4. 1字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5. 4字节的消息序号（int）
        out.writeInt(msg.getSequenceId());
        // 6. 1字节为了对其2的倍数填充字节
        out.writeByte(0xff);
        // 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 7. 4字节的信息长度（int）
        out.writeInt(bytes.length);
        // 8. 剩下都为消息内容
        out.writeBytes(bytes);
    }

    /**
     * 解码：将ByteBuf解析为消息
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 4字节的魔数
        byte[] magicBytes = new byte[4];
        in.readBytes(magicBytes);
        // 2. 1字节的版本
        byte version = in.readByte();
        // 3. 1字节的序列化方式（jdk-0，json-1）
        byte serializerType = in.readByte();
        // 4. 1字节的指令类型
        byte messageType = in.readByte();
        // 5. 4字节的消息序号（int）
        int sequenceId = in.readInt();
        // 6. 1字节为了对其2的倍数填充字节
        in.readByte();
        // 获取内容的字节数组
        // 7. 4字节的信息长度（int）
        int length = in.readInt();
        // 8. 剩下都为消息内容
        byte[] messageBytes = new byte[length];
        in.readBytes(messageBytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(messageBytes));
        Message message = (Message) ois.readObject();
        out.add(message);
        log.debug("magicNum: {}, version: {}, serializerType: {}, messageType: {}, sequenceId: {}, length: {}",
                magicBytes, version, serializerType, messageType, sequenceId, length);
        log.debug("message: {}", message);
    }
}
