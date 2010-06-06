package org.msgpack.rpc;

import java.io.IOException;
import java.util.List;

import org.msgpack.MessageConvertable;
import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.MessageUnpackable;
import org.msgpack.Packer;
import org.msgpack.Schema;
import org.msgpack.Unpacker;
import org.msgpack.schema.ClassSchema;
import org.msgpack.schema.FieldSchema;


public class Hoge implements MessagePackable, MessageConvertable, MessageUnpackable
{
    private static final ClassSchema _SCHEMA = (ClassSchema)Schema.load("(class Hoge (package org.msgpack.rpc) (field a int))");
    public static ClassSchema getSchema() { return _SCHEMA; }

    public Integer a;

    public Hoge() { }

    @Override
    public void messagePack(Packer _pk) throws IOException
    {
        _pk.packArray(1);
        FieldSchema[] _fields = _SCHEMA.getFields();
        _fields[0].getSchema().pack(_pk, a);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void messageConvert(Object obj) throws MessageTypeException
    {
        Object[] _source = ((List)obj).toArray();
        FieldSchema[] _fields = _SCHEMA.getFields();
        if(_source.length <= 0) { return; } this.a = (Integer)_fields[0].getSchema().convert(_source[0]);
    }

    @SuppressWarnings("unchecked")
    public static Hoge createFromMessage(Object[] _message)
    {
        Hoge _self = new Hoge();
        if(_message.length <= 0) { return _self; } _self.a = (Integer)_message[0];
        return _self;
    }

    @Override
    public void messageUnpack(Unpacker _upk) throws IOException, MessageTypeException {
        messageConvert(_upk.getData());
    }
}
