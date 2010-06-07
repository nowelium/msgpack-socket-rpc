package org.msgpack.rpc;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.MessageConvertable;
import org.msgpack.MessagePackable;
import org.msgpack.MessageTypeException;
import org.msgpack.MessageUnpackable;
import org.msgpack.Packer;
import org.msgpack.Unpacker;
import org.msgpack.schema.ClassSchema;
import org.msgpack.schema.FieldSchema;
import org.msgpack.schema.GenericSchema;
import org.msgpack.schema.IntSchema;
import org.msgpack.schema.SpecificClassSchema;
import org.msgpack.schema.StringSchema;

public class Response implements Serializable, MessagePackable, MessageConvertable, MessageUnpackable {

    private static final long serialVersionUID = 1L;
    
    private static final List<FieldSchema> _FIELDS = createFieldSchema();
    
    private static final ClassSchema _SCHEMA = createSchema();

    private MessageType type = MessageType.Response;

    private int id;
    
    private String errorMessage;

    private Object result;
    
    public MessageType getType(){
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
    
    protected static List<FieldSchema> createFieldSchema(){
        List<FieldSchema> fields = new ArrayList<FieldSchema>();
        fields.add(new FieldSchema("type", new IntSchema()));
        fields.add(new FieldSchema("id", new IntSchema()));
        fields.add(new FieldSchema("errorMessage", new StringSchema()));
        fields.add(new FieldSchema("result", new GenericSchema()));
        return fields;
    }
    
    protected static ClassSchema createSchema(){
        Class<?> clazz = Request.class;
        Package pkg = clazz.getPackage();
        return new SpecificClassSchema(clazz.getSimpleName(), pkg.getName(), null, _FIELDS);
    }
    
    @Override
    public void messagePack(Packer packer) throws IOException {
        packer.packArray(_FIELDS.size());
        FieldSchema[] fields = _SCHEMA.getFields();
        fields[0].getSchema().pack(packer, type.getValue());
        fields[1].getSchema().pack(packer, id);
        fields[2].getSchema().pack(packer, errorMessage);
        fields[3].getSchema().pack(packer, result);
    }

    @Override
    public void messageUnpack(Unpacker unpacker) throws IOException, MessageTypeException {
        // FIXME: Request#messageUnpack <-> Responae#messageUnpack
        messageConvert(unpacker.next().getData());
    }
    
    @Override
    public void messageConvert(Object data) throws MessageTypeException {
        Object[] source = ((List<?>) data).toArray();
        FieldSchema[] fields = _SCHEMA.getFields();
        this.type = MessageType.get((Integer) fields[0].getSchema().convert(source[0]));
        this.id = (Integer) fields[1].getSchema().convert(source[1]);
        if(null != source[2]){
            this.errorMessage = (String) fields[2].getSchema().convert(source[2]);
        }
        if(null != source[3]){
            Object _rs = fields[3].getSchema().convert(source[3]);
            if(_rs instanceof byte[]){
                this.result = new String((byte[]) _rs);
            } else if(_rs instanceof Byte[]){
                Byte[] _bytes = (Byte[]) _rs;
                byte[] buf = new byte[_bytes.length];
                for(int i = 0; i < _bytes.length; ++i){
                    buf[i] = _bytes[i].byteValue();
                }
                this.result = new String(buf);
            } else {
                this.result = _rs;
            }
        }
    }

}
