package utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ResetPolicy {
    Mode value() default Mode.INHERIT;

    enum Mode {
        NO_RESET,
        RESET_DATA,
        FAST_RESET,
        FULL_RESET,
        INHERIT
    }
}
