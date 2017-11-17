package main.java.framework.api.metrics;


public class CallCredentials {
    public String methodName;
    public String methodOwnerClass;
    public String callingMethod;
    public String callingClass;
    public String variableName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallCredentials that = (CallCredentials) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        if (methodOwnerClass != null ? !methodOwnerClass.equals(that.methodOwnerClass) : that.methodOwnerClass != null)
            return false;
        if (callingMethod != null ? !callingMethod.equals(that.callingMethod) : that.callingMethod != null)
            return false;
        return callingClass != null ? callingClass.equals(that.callingClass) : that.callingClass == null;
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + (methodOwnerClass != null ? methodOwnerClass.hashCode() : 0);
        result = 31 * result + (callingMethod != null ? callingMethod.hashCode() : 0);
        result = 31 * result + (callingClass != null ? callingClass.hashCode() : 0);
        return result;
    }
}
