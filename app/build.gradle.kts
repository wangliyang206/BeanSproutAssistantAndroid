plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.daggerHiltAndroid)
}

android {
    namespace = "com.wly.beansprout"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wly.beansprout"
        minSdk = 24
        targetSdk = 34
        versionCode = 158
        versionName = "1.5.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    //编译可选项参数
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        // ✅ 与 Kotlin 1.9.23 兼容的版本
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    //签名配置
    signingConfigs {
        //正式
        create("release") {
            // 从 local.properties 读取配置（类型安全，自动转换为 String）
            keyAlias = project.properties["SIGNING_KEY_ALIAS"] as String
            keyPassword = project.properties["SIGNING_KEY_PASSWORD"] as String
            storeFile = file(project.properties["SIGNING_STORE_FILE"] as String)
            storePassword = project.properties["SIGNING_STORE_PASSWORD"] as String

//            类型: X.509
//            版本: 3
//            序列号: 0x3084714a
//            主题: CN=beanSproutAssistant
//            有效期始: Sun Apr 28 11:07:59 CST 2024
//            有效期至: Thu Apr 22 11:07:59 CST 2049
//            公钥类型: RSA
//            指数: 65537
//            模数大小（位）: 2048
//            模数: 27421194092982756611668810619582134674648547099674199526599095878806795180688526964970927640996967195191347848222901966830511431079073705309557775802889083690871178931590697931143751019147021546947680921670947868187482602113508677191143333215037360551967750262101342205143955880892919793756759028224824236312650819995682900531224910219166787216610483869064835518183891232472655096295198244111572883577767306981779680297863909020834893397162705196879062936691878931958578248560996048385926541088954255696245426685722862983398954562474537340237138152352788396470630210506758582299091908775084561633501754520751467626749
//            签名算法: SHA256withRSA
//            签名 OID: 1.2.840.113549.1.1.11
//            MD5 签名: 13 07 3A 2E AE 03 6B 4A 91 24 54 67 03 73 21 FD
//            SHA-1 签名: C5 7E AD AB C3 C0 44 4A 18 50 B2 DE 37 D7 7C C8 79 FE 07 15
//            SHA-256 签名: D5 BC 46 0C 72 2C 98 01 9E 15 16 77 A6 EA BC FB E8 8D F3 92 BA C4 22 2F C9 56 01 D5 80 1C 85 91
        }
        //测试
        getByName("debug") {
            //签名文件路径
            storeFile = file(project.properties["TEST_SIGNING_STORE_FILE"] as String)

//            类型: X.509
//            版本: 1
//            序列号: 0x1
//            主题: C=US, O=Android, CN=Android Debug
//            有效期始: Wed Apr 27 13:40:34 CST 2016
//            有效期至: Fri Apr 20 13:40:34 CST 2046
//            公钥类型: RSA
//            指数: 65537
//            模数大小（位）: 1024
//            模数: 108013749204158993662062337814969006810694683320255903416664887221739365809869856789271555603147145426574347937033705257992255600089169702017451644203255969236007229283241578610220035532706109159091174088612647556652149037143061264704370196976690247478298077571636436861765011329803965444855584083714461784697
//            签名算法: SHA1withRSA
//            签名 OID: 1.2.840.113549.1.1.5
//            MD5 签名: B9 C4 DC 5B 1B E6 D5 4D AC 5B C3 0B F6 15 75 D9
//            SHA-1 签名: 60 B9 59 C0 EA C4 63 FA C3 E5 CE A0 E4 B7 F8 CE 49 EF 53 64
//            SHA-256 签名: 74 5E 5D 89 E7 A2 64 2A 92 69 92 0B 8A 5B 78 42 B1 03 30 0A 40 C3 FE 1B 6E 1D 73 40 75 2A E1 2A
        }
    }

    //构建类型(一般是release和debug，还可以自定义)
    buildTypes {
        //测试版
        debug {
            //默认false,是否开启断点调试
            isDebuggable = true

            //默认false,是否开启jni的断点调试
            isJniDebuggable = true

            //版本名的后缀
            versionNameSuffix = "-测试版"

            //applicationId的后缀,相当于更改了applicationId,可以认为是一个新的应用
            applicationIdSuffix = ".test"

            //配置签名方式，这里配置会覆盖defaultConfig中配置的签名方式
            signingConfig = signingConfigs.getByName("debug")

            //SP文件名称
            buildConfigField("String", "SHARED_NAME_INVEST", "\"sharedAssistantTest\"")

            //AndroidManifest中用到的配置
            manifestPlaceholders["UM_APP_KEY"] = "@string/um_app_key_manifest_debug"
        }

        //正式发布版
        release {
            //默认false,是否开启断点调试
            isDebuggable = false

            //默认false,是否混淆的开关
            isMinifyEnabled = true

            //加载默认混淆配置文件proguard-rules.pro
            proguardFile("proguard-rules.pro")

            //配置签名方式，这里配置会覆盖defaultConfig中配置的签名方式
            signingConfig = signingConfigs.getByName("release")

            //SP文件名称
            buildConfigField("String", "SHARED_NAME_INVEST", "\"sharedAssistantTest\"")

            //AndroidManifest中用到的配置
            manifestPlaceholders["UM_APP_KEY"] = "@string/um_app_key_manifest"
        }
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    // Jetpack Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // 协程
    implementation(libs.kotlinx.coroutines)

    // 网络请求
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okHttp3.logging.interceptor)
    implementation(libs.javax.inject)

    // UI
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.ui.googlefonts)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt（依赖注入，可选）
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // 友盟统计
    implementation(libs.umeng.common)
    implementation(libs.umeng.asms)
    implementation(libs.umeng.apm)
    implementation(libs.androidx.recyclerview)

    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

// 配置Hilt
kapt {
    //  错误类型
    correctErrorTypes = true
}
