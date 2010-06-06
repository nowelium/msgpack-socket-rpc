package org.msgpack.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.Packer;
import org.msgpack.Schema;
import org.msgpack.Unpacker;
import org.msgpack.schema.ClassGenerator;
import org.msgpack.schema.FieldSchema;
import org.msgpack.schema.IntSchema;
import org.msgpack.schema.SpecificClassSchema;

public class MessagePacker {
    
    protected static Schema createSchema(){
        List<FieldSchema> fields = new ArrayList<FieldSchema>();
        {
            FieldSchema f = new FieldSchema("a", new IntSchema());
            fields.add(f);
        }
        return new SpecificClassSchema("Hoge", Hoge.class.getPackage().getName(), null, fields);
    }
    
    public static void main(String...args){
        StringWriter w = new StringWriter();
        try {
            ClassGenerator.write(createSchema(), w);
            System.out.println(w.getBuffer().toString());
        } catch(IOException e){
        }
    }
    
    @Test
    public void messagePack() throws IOException {
        Hoge test1 = new Hoge();
        test1.a = 100;
        
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Packer packer = new Packer(o);
        packer.pack(test1);
        
        Unpacker unpacker = new Unpacker();
        unpacker.wrap(o.toByteArray());
        unpacker.execute();
        
        Hoge test2 = new Hoge();
        test2.messageUnpack(unpacker);
        
        Assert.assertEquals(test2.a, 100);
    }
    
}
