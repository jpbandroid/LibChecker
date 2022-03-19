import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.nio.charset.Charset

const val baseVersionName = "2.2.1"

fun Project.setupLibraryModule(block: LibraryExtension.() -> Unit = {}) {
  setupBaseModule(block)
}

fun Project.setupAppModule(block: BaseAppModuleExtension.() -> Unit = {}) {
  setupBaseModule<BaseAppModuleExtension> {
    defaultConfig {
      versionCode = 221
      versionName = "2.2.1_jpb"
      resourceConfigurations += arrayOf("en", "zh-rCN", "zh-rTW", "ru", "uk-rUA")
    }
    buildTypes {
      debug {
        applicationIdSuffix = ".debug"
      }
      release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
        )
      }
    }
    block()
  }
}

private inline fun <reified T : BaseExtension> Project.setupBaseModule(crossinline block: T.() -> Unit = {}) {
  extensions.configure<BaseExtension>("android") {
    compileSdkVersion(32)
    defaultConfig {
      minSdk = 23
      targetSdk = 32
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      freeCompilerArgs = freeCompilerArgs + arrayOf("-Xopt-in=kotlin.RequiresOptIn")
    }
    compileOptions {
      targetCompatibility(JavaVersion.VERSION_11)
      sourceCompatibility(JavaVersion.VERSION_11)
    }
    (this as T).block()
  }
}

private fun BaseExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
  (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

fun String.exec(): String = Runtime.getRuntime().exec(this).inputStream.readBytes()
  .toString(Charset.defaultCharset()).trim()