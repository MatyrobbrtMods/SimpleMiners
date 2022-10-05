package com.matyrobbrt.simpleminers.packsdatagen;

import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterPack {
    Type TYPE = Type.getType(RegisterPack.class);

    String value();
}
