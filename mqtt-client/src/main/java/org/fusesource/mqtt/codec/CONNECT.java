/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.mqtt.codec;

import org.fusesource.hawtbuf.*;
import org.fusesource.mqtt.client.QoS;

import java.io.IOException;
import java.net.ProtocolException;
import static org.fusesource.mqtt.codec.MessageSupport.*;

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class CONNECT implements Message {

    public static final byte TYPE = 1;
    
    private static final Buffer PROTOCOL_NAME = new AsciiBuffer("MQIsdp").buffer();
    private static final int PROTOCOL_VERSION = 3;

    private short keepAlive = 30;
    private UTF8Buffer clientId;
    private UTF8Buffer willTopic;
    private UTF8Buffer willMessage = new UTF8Buffer("");
    private boolean willRetain;
    private byte willQos;
    private boolean cleanSession = true;
    private UTF8Buffer userName;
    private UTF8Buffer password;
    private int version;


    public CONNECT(){
    }

    public CONNECT(CONNECT other) {
        this.keepAlive = other.keepAlive;
        this.clientId = other.clientId;
        this.willTopic = other.willTopic;
        this.willMessage = other.willMessage;
        this.willRetain = other.willRetain;
        this.willQos = other.willQos;
        this.cleanSession = other.cleanSession;
        this.userName = other.userName;
        this.password = other.password;
    }

    public byte messageType() {
        return TYPE;
    }

    public CONNECT decode(MQTTFrame frame) throws ProtocolException {
        assert(frame.buffers.length == 1);
        DataByteArrayInputStream is = new DataByteArrayInputStream(frame.buffers[0]);

        if( !PROTOCOL_NAME.equals(MessageSupport.readUTF(is)) ) {
            throw new ProtocolException("Invalid CONNECT encoding");
        }

        version = is.readByte() & 0xFF;
        byte flags = is.readByte();
        boolean username_flag = (flags & 0x80) > 0;
        boolean password_flag = (flags & 0x40) > 0;
        willRetain = (flags & 0x20) > 0;
        willQos = (byte) ((flags & 0x18) >>> 3);
        boolean will_flag = (flags & 0x04) > 0;
        cleanSession = (flags & 0x02) > 0;

        keepAlive = is.readShort();
        clientId = MessageSupport.readUTF(is);
        if(will_flag) {
            willTopic = MessageSupport.readUTF(is);
            willMessage = MessageSupport.readUTF(is);
        }
        if( username_flag ) {
            userName = MessageSupport.readUTF(is);
        }
        if( password_flag ) {
            password = MessageSupport.readUTF(is);
        }
        return this;
    }
    
    public MQTTFrame encode() {
        try {
            DataByteArrayOutputStream os = new DataByteArrayOutputStream(500);
            MessageSupport.writeUTF(os, PROTOCOL_NAME);
            os.writeByte(PROTOCOL_VERSION);
            int flags = 0;
            if(userName!=null) {
                flags |= 0x80;
            }
            if(password!=null) {
                flags |= 0x40;
            }
            if(willTopic!=null && willMessage!=null) {
                flags |= 0x04;
                if(willRetain) {
                    flags |= 0x20;
                }
                flags |= (willQos << 3) & 0x18;
            }
            if(cleanSession) {
                flags |= 0x02;
            }
            os.writeByte(flags);
            os.writeShort(keepAlive);
            MessageSupport.writeUTF(os, clientId);
            if(willTopic!=null && willMessage!=null) {
                MessageSupport.writeUTF(os, willTopic);
                MessageSupport.writeUTF(os, willMessage);
            }
            if(userName!=null) {
                MessageSupport.writeUTF(os, userName);
            }
            if(password!=null) {
                MessageSupport.writeUTF(os, password);
            }

            MQTTFrame frame = new MQTTFrame();
            frame.commandType(TYPE);
            return frame.buffer(os.toBuffer());
        } catch (IOException e) {
            throw new RuntimeException("The impossible happened");
        }
    }

    public boolean cleanSession() {
        return cleanSession;
    }

    public CONNECT cleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public UTF8Buffer clientId() {
        return clientId;
    }

    public CONNECT clientId(UTF8Buffer clientId) {
        this.clientId = clientId;
        return this;
    }

    public short keepAlive() {
        return keepAlive;
    }

    public CONNECT keepAlive(short keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public UTF8Buffer password() {
        return password;
    }

    public CONNECT password(UTF8Buffer password) {
        this.password = password;
        return this;
    }

    public UTF8Buffer userName() {
        return userName;
    }

    public CONNECT userName(UTF8Buffer userName) {
        this.userName = userName;
        return this;
    }

    public UTF8Buffer willMessage() {
        return willMessage;
    }

    public CONNECT willMessage(UTF8Buffer willMessage) {
        this.willMessage = willMessage;
        return this;
    }

    public QoS willQos() {
        return QoS.values()[willQos];
    }

    public CONNECT willQos(QoS willQos) {
        this.willQos = (byte) willQos.ordinal();
        return this;
    }

    public boolean willRetain() {
        return willRetain;
    }

    public CONNECT willRetain(boolean willRetain) {
        this.willRetain = willRetain;
        return this;
    }

    public UTF8Buffer willTopic() {
        return willTopic;
    }

    public CONNECT willTopic(UTF8Buffer willTopic) {
        this.willTopic = willTopic;
        return this;
    }

    public int version() {
        return version;
    }

    public CONNECT version(int version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        return "CONNECT{" +
                "cleanSession=" + cleanSession +
                ", keepAlive=" + keepAlive +
                ", clientId=" + clientId +
                ", willTopic=" + willTopic +
                ", willMessage=" + willMessage +
                ", willRetain=" + willRetain +
                ", willQos=" + willQos +
                ", userName=" + userName +
                ", password=" + password +
                '}';
    }
}
