# Lib Installer
*An easy solution for developers to install XanderLib/ModCore at runtime.*

## How To Use
```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'uk.co.isXander:lib-installer:1.0'
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