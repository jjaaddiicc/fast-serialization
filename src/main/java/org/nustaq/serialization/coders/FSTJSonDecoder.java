package org.nustaq.serialization.coders;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import jdk.nashorn.internal.parser.JSONParser;
import org.nustaq.serialization.*;
import org.nustaq.serialization.minbin.MBIn;
import org.nustaq.serialization.minbin.MBObject;
import org.nustaq.serialization.minbin.MinBin;
import org.nustaq.serialization.util.FSTInputStream;
import org.nustaq.serialization.util.FSTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by moelrue on 5/21/15.
 */
public class FSTJSonDecoder implements FSTDecoder {

    FSTConfiguration conf;

    JsonParser input;

    private FSTInputStream fstInput;

    public FSTJSonDecoder(FSTConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public String readStringUTF() throws IOException {
        String res = input.nextFieldName();
        if ( res == null )
            return input.getText();
        return res;
//        return input.nextTextValue();
//        Object read = input.readObject();
//        if (read instanceof String)
//            return (String) read;
//        // in case preceding atom has been consumed b[] => str 8 char[] => str 16;
//        if (read instanceof byte[]) {
//            return new String((byte[]) read, 0, 0, ((byte[]) read).length);
//        } else if (read instanceof char[]) {
//            return new String((char[]) read, 0, ((char[]) read).length);
//        } else if (MinBin.END_MARKER == read) {
//            return null;
//        } else if ( read == null )
//            return null;
//        throw new RuntimeException("Expected String, byte[], char[] or tupel end");
    }

    @Override
    public String readStringAsc() throws IOException {
        return (String) input.nextTextValue();
    }

    @Override
    /**
     * if array is null => create own array. if len == -1 => use len read
     */
    public Object readFPrimitiveArray(Object array, Class componentType, int len) {
        try {
            if (componentType == double.class) {
                double[] da = (double[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getDoubleValue();
                }
                return da;
            }
            if (componentType == float.class) {
                float[] da = (float[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken();
                    da[i] = input.getFloatValue();
                }
                return da;
            }
            Object arr = array;
            int length = Array.getLength(arr);
            if (len != -1 && len != length)
                throw new RuntimeException("unexpected arrays size");
            if (componentType == boolean.class) {
                boolean[] da = (boolean[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getBooleanValue();
                }
                return da;
            }
            else if (componentType == byte.class) {
                byte[] da = (byte[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getByteValue();
                }
                return da;
            }
            else if (componentType == short.class) {
                short[] da = (short[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getShortValue();
                }
                return da;
            }
            else if (componentType == char.class) {
                char[] da = (char[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = (char) input.getIntValue();
                }
                return da;
            }
            else if (componentType == int.class) {
                int[] da = (int[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = (int) input.getIntValue();
                }
                return da;
            }
            else if (componentType == long.class) {
                long[] da = (long[]) array;
                for (int i = 0; i < da.length; i++) {
                    input.nextToken(); da[i] = input.getLongValue();
                }
                return da;
            }
            else throw new RuntimeException("unsupported type " + componentType.getName());
        } catch (Exception e) {
            throw FSTUtil.rethrow(e);
        }
    }

    @Override
    public void readFIntArr(int len, int[] arr) throws IOException {
        JsonToken jsonToken = input.nextToken();
        if ( ! jsonToken.isStructStart() )
            throw new RuntimeException("Expected array start");
        for (int i = 0; i < len; i++) {
            input.nextToken(); arr[i] = input.getIntValue();
        }
    }

    @Override
    public int readFInt() throws IOException {
        return input.nextIntValue(-1);
    }

    @Override
    public double readFDouble() throws IOException {
        input.nextToken();
        return input.getDoubleValue();
    }

    @Override
    public float readFFloat() throws IOException {
        input.nextToken();
        return input.getFloatValue();
    }

    @Override
    public byte readFByte() throws IOException {
        input.nextToken();
        return input.getByteValue();
    }

    @Override
    public int readIntByte() throws IOException {
        input.nextToken();
        return input.getByteValue();
    }

    @Override
    public long readFLong() throws IOException {
        input.nextToken();
        return input.getLongValue();
    }

    @Override
    public char readFChar() throws IOException {
        input.nextToken();
        return (char) input.getIntValue();
    }

    @Override
    public short readFShort() throws IOException {
        input.nextToken();
        return input.getShortValue();
    }

    @Override
    public int readPlainInt() throws IOException {
        throw new RuntimeException("not supported");
    }

    @Override
    public byte[] getBuffer() {
        return fstInput.buf;
    }

    @Override
    public int getInputPos() {
        return fstInput.pos;
    }

    @Override
    public void moveTo(int position) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void setInputStream(InputStream in) {
        try {
            fstInput = new FSTInputStream(in);
            input = FSTJSonEncoder.fac.createParser(fstInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int ensureReadAhead(int bytes) {
        return 0;
    }

    @Override
    public void reset() {
        fstInput.reset();
    }

    @Override
    public void resetToCopyOf(byte[] bytes, int off, int len) {
        if (off != 0 )
            throw new RuntimeException("not supported");
        byte b[] = new byte[len];
        System.arraycopy(bytes,off,b,0,len);
        fstInput = new FSTInputStream(b);
        try {
            input = FSTJSonEncoder.fac.createParser(fstInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetWith(byte[] bytes, int len) {
        fstInput = new FSTInputStream(bytes,0,len);
        try {
            input = FSTJSonEncoder.fac.createParser(fstInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getObjectHeaderLen() // len field of last header read (if avaiable)
    {
        return lastObjectLen;
    }

    int lastObjectLen;
    Class lastDirectClass;
    public byte readObjectHeaderTag() throws IOException {
        lastObjectLen = -1;
        lastReadDirectObject = null;
        lastDirectClass = null;
        JsonToken jsonToken = input.nextToken();
        if ( jsonToken == JsonToken.VALUE_STRING ) {
            lastReadDirectObject = input.getText();
            return FSTObjectOutput.DIRECT_OBJECT;
        }
        if ( jsonToken == JsonToken.VALUE_NUMBER_INT ) {
            lastReadDirectObject = input.getLongValue();
            return FSTObjectOutput.DIRECT_OBJECT;
        }
        if ( jsonToken == JsonToken.VALUE_NUMBER_FLOAT ) {
            lastReadDirectObject = input.getDoubleValue();
            return FSTObjectOutput.DIRECT_OBJECT;
        }
        if ( jsonToken == JsonToken.START_ARRAY ) {
            List arrayTokens = new ArrayList();
            JsonToken elem = input.nextToken();
            while ( ! elem.isStructEnd() ) {
                if ( elem == JsonToken.VALUE_NUMBER_INT ) {
                    arrayTokens.add(input.getLongValue());
                } else if ( elem == JsonToken.VALUE_NUMBER_FLOAT ) {
                    arrayTokens.add(input.getDoubleValue());
                }
                elem = input.nextValue();
            }
            lastReadDirectObject = arrayTokens;
            return FSTObjectOutput.DIRECT_ARRAY_OBJECT;
        }
        if ( jsonToken != JsonToken.START_OBJECT ) {
            throw new RuntimeException("Expected Object start, got '"+jsonToken+"'");
        }

        String typeTag = input.nextFieldName();
        if ( typeTag.equals("type") ) {
            // object
            String type = input.nextTextValue();
            String valueTag = input.nextFieldName();
            if ( ! "value".equals(valueTag) ) {
                throw new RuntimeException("expected value attribute for object of type:"+type);
            }
            if ( ! input.nextToken().isStructStart() ) {
                throw new RuntimeException("expected struct start");
            }
            try {
                lastDirectClass = classForName(conf.getClassForCPName(type));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return FSTObjectOutput.OBJECT;
        } else if ( typeTag.equals("seqType") ) {
            // sequence
            String type = input.nextTextValue();
            try {
                lastDirectClass = classForName(conf.getClassForCPName(type));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return FSTObjectOutput.ARRAY;
        } else if ( typeTag.equals("ref") ) {
            // ref
        }
//        byte tag = input.peekIn();
//        lastDirectClass = null;
//        if ( MinBin.isTag(tag) ) {
//            if ( MinBin.getTagId(tag) == MinBin.HANDLE ) {
//                input.readIn(); // consume
//                return FSTObjectOutput.HANDLE;
//            }
//            if ( MinBin.getTagId(tag) == MinBin.STRING )
//                return FSTObjectOutput.STRING;
//            if ( MinBin.getTagId(tag) == MinBin.BOOL ) {
//                Boolean b = (Boolean) input.readObject();
//                return b ? FSTObjectOutput.BIG_BOOLEAN_TRUE : FSTObjectOutput.BIG_BOOLEAN_FALSE;
//            }
//            if (    MinBin.getTagId(tag) == MinBin.DOUBLE ||
//                    MinBin.getTagId(tag) == MinBin.DOUBLE_ARR ||
//                    MinBin.getTagId(tag) == MinBin.FLOAT_ARR ||
//                    MinBin.getTagId(tag) == MinBin.FLOAT
//                    )
//            {
//                lastReadDirectObject = input.readObject();
//                return FSTObjectOutput.DIRECT_OBJECT;
//            }
//            input.readIn();
//            if (MinBin.getTagId(tag) == MinBin.SEQUENCE) {
//                try {
//                    String cln = (String) input.readObject();
//                    {
//                        lastDirectClass = conf.getClassRegistry().classForName(conf.getClassForCPName(cln));
//                    }
//                } catch (ClassNotFoundException e) {
//                    throw FSTUtil.rethrow(e);
//                }
//                if ( lastDirectClass.isEnum() ) {
//                    input.readInt(); // consume length of 1
//                    String enumString = (String) input.readObject();
//                    lastReadDirectObject = Enum.valueOf(lastDirectClass,enumString);
//                    lastDirectClass = null;
//                    return FSTObjectOutput.DIRECT_OBJECT;
//                } else
//                if ( lastDirectClass.isArray() )
//                    return FSTObjectOutput.ARRAY;
//                else {
//                    input.readInt(); // consume -1 for unknown sequence length
//                    return FSTObjectOutput.OBJECT; // with serializer
//                }
//            }
//            if (MinBin.getTagId(tag)==MinBin.NULL)
//                return FSTObjectOutput.NULL;
//            return FSTObjectOutput.OBJECT;
//        }
//        lastReadDirectObject = input.readObject();
//        return FSTObjectOutput.DIRECT_OBJECT;
        throw new RuntimeException("expected object header");
    }

    public Object getDirectObject() // in case class already resolves to read object (e.g. mix input)
    {
        Object tmp = lastReadDirectObject;
        lastReadDirectObject = null;
        return tmp;
    }

    Object lastReadDirectObject; // in case readClass already reads full minbin value
    @Override
    public FSTClazzInfo readClass() throws IOException, ClassNotFoundException {
        if (lastDirectClass != null ) {
            FSTClazzInfo clInfo = conf.getCLInfoRegistry().getCLInfo(lastDirectClass);
            lastDirectClass = null;
            return clInfo;
        }
//        Object read = input.readObject();
//        String name = (String) read;
//        String clzName = conf.getClassForCPName(name);
//        return conf.getCLInfoRegistry().getCLInfo(classForName(clzName));
        return null;
    }

    HashMap<String,Class> clzCache = new HashMap<>();
    @Override
    public Class classForName(String name) throws ClassNotFoundException {
        if ("Object".equals(name))
            return MBObject.class;
        Class aClass = clzCache.get(name);
        if (aClass!=null)
            return aClass;
        aClass = Class.forName(name);
        clzCache.put(name,aClass);
        return aClass;
    }

    @Override
    public void registerClass(Class possible) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() {
        //TODO
        throw new RuntimeException("not implemented");
    }

    @Override
    public void skip(int n) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void readPlainBytes(byte[] b, int off, int len) {
        try {
            for (int i = 0; i < len; i++) {
                input.nextToken();
                b[i+off] = input.getByteValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isMapBased() {
        return true;
    }

    public void consumeEndMarker() {
        try {
            if ( ! input.nextToken().isStructEnd() ) {
                throw new RuntimeException("end of structure expected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class readArrayHeader() throws Exception {
        readObjectHeaderTag();
        return lastDirectClass;
//        byte tag = input.peekIn(); // need to be able to consume MinBin Sequence tag silently
//        if ( MinBin.getTagId(tag) == MinBin.NULL ) {
//            input.readIn();
//            lastDirectClass = null;
//            return null;
//        }
//        if ( lastDirectClass != null )
//            return readClass().getClazz();
//        if ( MinBin.getTagId(tag) == MinBin.SEQUENCE ) {
//            input.readIn(); // consume (multidim array)
//        } else if ( MinBin.isPrimitive(tag) ) {
//            input.readIn(); // consume tag
//            switch (MinBin.getBaseType(tag)) {
//                case MinBin.INT_8:
//                    return byte[].class;
//                case MinBin.INT_16:
//                    if (MinBin.isSigned(tag) )
//                        return short[].class;
//                    return char[].class;
//                case MinBin.INT_32:
//                    return int[].class;
//                case MinBin.INT_64:
//                    return long[].class;
//            }
//        }
//        return readClass().getClazz();
    }

    @Override
    public void readExternalEnd() {
        consumeEndMarker();
    }

    @Override
    public boolean isEndMarker(String s) {
        return s.equals("}") || s.equals("]");
    }

    @Override
    public int readVersionTag() throws IOException {
        return 0; // versioning not supported for json
    }

    @Override
    public void pushBack(int bytes) {
        //fstInput.psetPos(input.getPos()-bytes);
        throw new RuntimeException("not supported");
    }


}
