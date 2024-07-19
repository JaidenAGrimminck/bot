package me.autobot.lib.logging;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public interface Logger {
    //get all @Log annotated fields and methods
    
    private HashMap<String, String> getLogFields() {
        HashMap<String, String> logFields = new HashMap<>();

        //get all fields annotated with @Log
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Log.class)) {
                Log log = field.getAnnotation(Log.class);
                
                String as = log.as();

                if (as.isEmpty()) {
                    as = field.getName();
                }
                
                boolean isAccessible = field.isAccessible();

                //set access to public
                field.setAccessible(true);

                try {
                    logFields.put(as, field.get(this).toString());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                field.setAccessible(isAccessible);
            }
        }
    
        //get all methods annotated with @Log
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(Log.class)) {
                Log log = method.getAnnotation(Log.class);
                
                String as = log.as();

                if (as.isEmpty()) {
                    as = method.getName();
                }
                
                boolean isAccessible = method.isAccessible();

                //set access to public
                method.setAccessible(true);

                try {
                    logFields.put(as, method.invoke(this).toString());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                method.setAccessible(isAccessible);
            }
        }
    
        return logFields;
    }
}
