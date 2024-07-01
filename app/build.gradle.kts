import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

android {
    namespace = "com.intech.spins"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.intech.spins"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    productFlavors {
        create("spinskkk") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinskkk.com/?apk=spinskkk\"")
        }
        create("spinspee") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinspee.com/?apk=spinspee\"")
        }
        create("spinsabc") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinsabc.space/?invite=spinsabc\"")
        }
        create("spinswintv") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinswin.tv/?invite=spinswintv\"")
        }
        create("spinsjilitv") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinsjili.tv/?invite=spinsjilitv\"")
        }
        create("jilispinstv") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://jilispins.tv/?invite=jilispinstv\"")
        }
        create("spinsslotwin") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinsslot.win/?invite=spinsslotwin\"")
        }
        create("spinssabwin") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinssab.win/?invite=spinssabwin\"")
        }
        create("spinsjiliwin") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinsjili.win/?invite=\"")
        }
        create("slotprobuzz") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://slotpro.buzz/?invite=spinsjiliwin\"")
        }
        create("phbeteu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://phbet.eu/?invite=phbeteu\"")
        }
        create("phwineu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://phwin.eu/?invite=phwineu\"")
        }
        create("phbet99eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://phbet99.eu/?invite=phbet99eu\"")
        }
        create("phbet88eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://phbet88.eu/?invite=phbet88eu\"")
        }
        create("sp77eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://sp77.eu/?invite=sp77eu\"")
        }
        create("sp128eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://sp128.eu/?invite=sp128eu\"")
        }
        create("sp777eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://sp777.eu/?invite=sp777eu\"")
        }
        create("sp888eu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://sp888.eu/?invite=sp888eu\"")
        }
        create("88pheu") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://88ph.eu/?invite=88pheu\"")
        }
        create("tuuimgg") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://tuuimgg.com/?invite=tuuimgg\"")
        }
        create("spinsany") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://spinsany.net/?invite=spinsany\"")
        }
        create("spinswho") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://spinswho.com/?invite=spinswho\"")
        }
        create("spins123space") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spins123.space/?invite=spins123space\"")
        }
        create("spins999space") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://spins999.space/?invite=spins999space\"")
        }
        create("spinsps5vip") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://spinsps5.vip/?invite=spinsps5vip\"")
        }
        create("slots777io") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://slots777.io/?invite=slots777io\"")
        }
        create("spins777vip") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"http://spins777.vip?invite=spins777vip\"")
        }
        create("spinsphcom") {
            dimension = "version"
            buildConfigField("String", "DOMAIN", "\"https://spinsph.com/?invite=spinsphcom\"")
        }
    }
    flavorDimensions("version")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // 设置 APK 文件名
    applicationVariants.all { variant ->
        variant.outputs.all { output ->
            val date = Date()
            val formattedDate = SimpleDateFormat("yyyyMMdd-HHmmss").format(date)
            val buildType = variant.buildType.name
            val productFlavor = variant.productFlavors[0].name
            val fileName = "$productFlavor-$buildType-$formattedDate.apk"
            val outFile = output.outputFile
            if (outFile != null && outFile.name.endsWith(".apk")) {
                val file = File(outFile.parent, fileName)
                if (file.exists()) {
                    file.delete()
                }
                outFile.renameTo(file)
            }
            true
        }
        true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 基础依赖包，必须要依赖
    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
// kotlin扩展（可选）
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-messaging:23.0.0")
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-inappmessaging-display:20.1.1")


    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.github.liujingxing.rxhttp:rxhttp:3.2.7")
    ksp ("com.github.liujingxing.rxhttp:rxhttp-compiler:3.2.7")
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")

}