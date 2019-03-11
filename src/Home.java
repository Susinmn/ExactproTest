import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

interface Encoder {
    byte[] serialize(Object anyBean) throws InvalidClassException;
    Object deserialize(byte[] data) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException;
}

public class Home implements Encoder {

    @Override
    public byte[] serialize(Object anyBean) throws InvalidClassException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(anyBean);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {

            return null;

        }

    }

    @Override
    public Object deserialize(byte[] data) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {

            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();

        } catch (IOException e) {
            
            return null;

        }
    }
}