# Lib Installer
*An easy solution for developers to install XanderLib/ModCore at runtime.*

## How To Use
```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    // you will need to shadow/embed this dependency
    implementation 'uk.co.isXander:lib-installer:-SNAPSHOT'
}

jar {
    manifest {
        attributes (
                // ------------------------------
                // Install XanderLib
                'FMLCorePlugin': 'co.uk.isxander.libinstaller.fml.XanderLib',
                // Install ModCore
                'FMLCorePlugin': 'co.uk.isxander.libinstaller.fml.ModCore',
                // ------------------------------
                'FMLCorePluginContainsFMLMod': true,
                'ForceLoadAsMod': true,
                'ModSide': 'CLIENT'
        )
    }
}
```
