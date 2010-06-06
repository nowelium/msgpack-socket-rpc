package org.msgpack.rpc;

import java.io.IOException;

import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.Packer;

public enum MessageType implements MessagePackable {
    Response(0),
    Request(1),
    Notification(2),
    ;
    
    private final int value;
    private MessageType(int value){
        this.value = value;
    }
    public int getValue(){
        return value;
    }
    
    @Override
    public void messagePack(Packer packer) throws IOException {
        packer.packInt(value);
    }
    
    /*
     * generated code
     * void messageUnpack
     * final fields update! 
     */
    
    public static MessageType get(final int val) throws MessageTypeException {
        switch(val){
        case 0:
            return Response;
        case 1:
            return Request;
        case 2:
            return Notification;
        default:
            throw new MessageTypeException();
        }
    }
    
}
