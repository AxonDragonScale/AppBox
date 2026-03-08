# KMP Migration

- Generate a KMP Project using the wizard
- It will have two module composeApp and iosApp

## ComposeApp
- Rename composeApp to myappComposeApp
- Fix dependencies in build.gradle.kts
- Update package name used for kmp resources and its usages

## iosApp
- Rename iosApp to myappIosApp
- In myappIosApp make sure all files/folders/strings are changed from iosApp to myappIosApp
- Rename iosApp.swift to MyappIosApp.swift
- Update `import ComposeApp` to `import LimitlessComposeApp`
- Update the KMP build script - cd to root folder with gradlew and update the gradle task path
```
cd "$SRCROOT/../../.."
./gradlew :apps:limitless:limitlessComposeApp:embedAndSignAppleFrameworkForXcode
```

## Multiple KMP Modules
- Not officially supported
- Add project in .idea/xcode.xml. Only one at a time, comment the rest
