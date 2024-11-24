plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
    kotlin("kapt")
}

val releaseVersion = "0.0.5"
val artifactIdentification = "dandamon-time"
val githubOwner = "DSDrachmann"
val githubProjectName = "TimerSDK"

android {
    namespace = "com.Dandd.time"
    compileSdk = 34

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)// Add room-ktx dependency
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publishing {
            repositories {
                maven {
                    name = "GitHubPackages"

                    url = uri("https://maven.pkg.github.com/${githubOwner}/${githubProjectName}")
                    credentials {
                        username = project.findProperty("gpr.user")?.toString()
                            ?: System.getenv("USERNAME")
                        password =
                            project.findProperty("gpr.key")?.toString() ?: System.getenv("TOKEN")
                    }
                }
            }
            publications {
                create<MavenPublication>("gpr") {
                    from(components["release"])
                    version = releaseVersion
                    artifactId = artifactIdentification
                }
            }
            /*           publications {
                           register("release", MavenPublication::class) {
                               groupId = "com.github.dsdrachmann"
                               artifactId = "favorite-sdk"
                               version = "0.0.3"

                               from(components["release"])
                           }
                           create<MavenPublication>("debug") {
                               groupId = "com.github.dsdrachmann"
                               artifactId = "favorite-sdk"
                               version = "0.0.3"

                               from(components["debug"])
                           }
                       }

             */
        }
    }
}
