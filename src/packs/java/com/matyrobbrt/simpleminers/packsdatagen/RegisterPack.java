package com.matyrobbrt.simpleminers.packsdatagen;

import org.objectweb.asm.Type;

public @interface RegisterPack {
    Type TYPE = Type.getType(RegisterPack.class);

    String value();
}
