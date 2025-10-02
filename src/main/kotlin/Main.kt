package nick.mirosh

import nick.mirosh.di.appModule
import org.koin.core.context.GlobalContext.startKoin



fun main() {
    startKoin {
        modules(appModule)
    }
}
