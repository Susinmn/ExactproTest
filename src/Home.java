import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

interface Encoder {
    byte[] serialize(Object anyBean) throws InvalidClassException;
    Object deserialize(byte[] data) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException;
}

class TestSerBean implements Serializable {
    private int ver;
    private String scope;
    private TestSerBean companionBean;
    private List<TestSerBean> listBean;
    private Set<TestSerBean> setBean;
    private Map<String, TestSerBean> mapBean;

    private static final long serialVersionUID = 1L;

    public TestSerBean(){}

    public void setVer(int ver) {
        this.ver = ver;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setCompanionBean(TestSerBean companionBean) {
        this.companionBean = companionBean;
    }

    public void setListBean(List<TestSerBean> listBean) {
        this.listBean = listBean;
    }
}

public class Home implements Encoder {
    public Home() {}

    public static void main(String[] args) {

        Home home = new Home();

        TestSerBean testCompanionBean = new TestSerBean();
        testCompanionBean.setVer(2);
        testCompanionBean.setScope("view");

        TestSerBean testSerBean = new TestSerBean();

        testSerBean.setVer(1);
        testSerBean.setScope("view");
        testSerBean.setCompanionBean(testCompanionBean);

        //testCompanionBean.setCompanionBean(testSerBean);

        List<TestSerBean> listBean = new ArrayList<>();
        listBean.add(testCompanionBean);
        testSerBean.setListBean(listBean);


        if (home.hasCyclicDependencies(testSerBean)) {
            throw new NullPointerException("Object has cyclical dependencies");
        } else {

            byte[] data = null;

            try {

                data = home.serialize(testSerBean);
                String serializeStr = new String(data);
                System.out.println(serializeStr);

            } catch (InvalidClassException e) {
                System.out.println("InvalidClassException");
            }

            try {

                System.out.println(home.deserialize(data));

            } catch (ClassNotFoundException e) {
                System.out.println("ClassNotFoundException");
            } catch (InvalidClassException e) {
                System.out.println("InvalidClassException");
            } catch (StreamCorruptedException e) {
                System.out.println("StreamCorruptedException");
            }

        }

    }

    @Override
    public byte[] serialize(Object anyBean) throws InvalidClassException {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(anyBean);

            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {

            System.out.println("IOException");
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

    public boolean hasCyclicDependencies(Object anyBean) {
        HashSet<Object> dependenciesSet = new HashSet<>();

        try {
            return checkDependencies(anyBean, dependenciesSet);
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException");
            return false;
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException");
            return false;
        }

    }

    private boolean checkDependencies(Object anyBean, HashSet<Object> dependenciesSet) throws IllegalArgumentException,IllegalAccessException {

        dependenciesSet.add(anyBean);

        //Set of adjacent vertices for the anyBean
        HashSet<Object> dependenciesThisBeanSet = new HashSet<>();

        //Search all attributes of the anyBean
        Field[] fields = anyBean.getClass().getDeclaredFields();
        //for (int i = 0; i < fields.length; i++) {
        for (Field field : fields) {
            field.setAccessible(true);
            Object object = field.get(anyBean);
            if (object != null) {
                String objectClass = object.getClass().toString();
                //Here we believe that the class name begins with a capital letter :)
                String objectClassName = objectClass.split(" ")[1];
                if (objectClassName.substring(0, 1).equals(objectClassName.substring(0, 1).toUpperCase())) {
                    if (!object.equals(anyBean)) {
                        //"not anyBean<->anyBean"
                        dependenciesThisBeanSet.add(object);
                    }
                } else if (object instanceof List) {
                    for (Object listObject : (List)object) {
                        if (!listObject.equals(anyBean)) {
                            //"not anyBean<->anyBean"
                            dependenciesThisBeanSet.add(listObject);
                        }
                    }
                } else if (object instanceof Set) {
                    for (Object setObject : (Set)object) {
                        if (!setObject.equals(anyBean)) {
                            //"not anyBean<->anyBean"
                            dependenciesThisBeanSet.add(setObject);
                        }
                    }
                } else if (object instanceof Map) {
                    for (Map.Entry <?, ?> mapObject: ((Map<?, ?>) object).entrySet()) {
                        if (!mapObject.getValue().equals(anyBean)) {
                            //"not anyBean<->anyBean"
                            dependenciesThisBeanSet.add(mapObject.getValue());
                        }
                    }
                }
            }
        }

        for (Object dependenciesThisBean : dependenciesThisBeanSet) {

            if (dependenciesSet.contains(dependenciesThisBean)) {
                return true;
            } else if (checkDependencies(dependenciesThisBean, dependenciesSet)) {
                return true;
            }

        }
        return false;
    }

}