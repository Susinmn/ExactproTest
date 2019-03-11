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