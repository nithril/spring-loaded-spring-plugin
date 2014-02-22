package org.nigajuan.springloaded;

/**
 * Created by nigajuan on 13/02/14.
 */
public class ToReloadBean {


    private final String typename;
    private final Class<?> clazz;
    private final String encodedTimestamp;


    public ToReloadBean(String typename, Class<?> clazz, String encodedTimestamp) {
        this.typename = typename;
        this.clazz = clazz;
        this.encodedTimestamp = encodedTimestamp;
    }

    public String getTypename() {
        return typename;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getEncodedTimestamp() {
        return encodedTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToReloadBean that = (ToReloadBean) o;

        if (!clazz.equals(that.clazz)) return false;
        if (!typename.equals(that.typename)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = typename.hashCode();
        result = 31 * result + clazz.hashCode();
        return result;
    }

}
