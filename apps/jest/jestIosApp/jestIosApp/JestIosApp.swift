import SwiftUI
import JestComposeApp

@main
struct JestIosApp: App {

    init() {
        KoinInitKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
