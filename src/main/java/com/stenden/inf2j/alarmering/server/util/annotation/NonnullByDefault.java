package com.stenden.inf2j.alarmering.server.util.annotation;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Nonnull
@TypeQualifierDefault({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {

}
