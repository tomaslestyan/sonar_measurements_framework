package main.java.framework.db;

/**
 * @author Filip Čekovský (433588)
 * @version 26.05.2017
 */

/**
 * Enum handling conversion between boolean and int throughout the database
 */
public enum DatabaseBoolean {
    FALSE(0),
    TRUE(1);

    private final int value;

    DatabaseBoolean(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public boolean getBoolean(){
        return this.name().equals(TRUE.name());
    }
}
