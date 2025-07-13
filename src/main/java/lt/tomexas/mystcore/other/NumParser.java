package lt.tomexas.mystcore.other;

public class NumParser {

    public static int asInt(Object val) {
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) return Integer.parseInt((String) val);
        throw new IllegalArgumentException("Cannot convert to int: " + val);
    }

    public static double asDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) return Double.parseDouble((String) val);
        throw new IllegalArgumentException("Cannot convert to double: " + val);
    }

    public static float asFloat(Object val) {
        if (val instanceof Float) return (Float) val;
        if (val instanceof Number) return ((Number) val).floatValue();
        if (val instanceof String) return Float.parseFloat((String) val);
        throw new IllegalArgumentException("Cannot convert to float: " + val);
    }

}
