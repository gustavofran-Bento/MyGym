// Arquivo de build raiz: apenas declara os plugins usados pelo módulo app.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
