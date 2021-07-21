import org.gradle.api.JavaVersion

object Versions {
    object Kotlin {
        const val lang = "1.5.10"
        const val coroutines = "1.5.0"
        const val serialization = "1.2.1"
    }

    object Java {
        const val jvmTarget = "11"
        val java = JavaVersion.VERSION_11
    }

    const val ksp = "1.5.10-1.0.0-beta02"
    const val agp = "7.0.0-beta05"
    const val spotless = "5.12.5"
    const val ktlint = "0.41.0"
    const val hilt = "2.37"
    const val okhttp = "4.9.1"
    const val retrofit2 = "2.9.0"
    const val hson = "0.1.4"
    const val compose = "1.0.0-rc02"
    const val constraintLayout = "1.0.0-alpha07"
    const val paging = "3.1.0-alpha02"
    const val paging_compose = "1.0.0-alpha10"
    const val activity = "1.3.0-rc02"
    const val datastore = "1.0.0-rc01"
    const val androidx_hilt = "1.0.0"
    const val room = "2.4.0-alpha03"
    const val lifecycle = "2.4.0-alpha02"
    const val lifecycle_compose = "1.0.0-alpha07"
    const val work = "2.7.0-alpha04"
    const val placeholder = "0.7.0"
    const val zoomable = "1.0.1"
    const val swiper = "0.6.0"
    const val nestedScrollView = "0.7.0"
    const val startup = "1.1.0-alpha01"
    const val coil = "1.3.0"
    const val accompanist = "0.14.0"
    const val androidx_exifinterface = "1.3.2"
    const val exoplayer = "2.14.1"
    const val browser = "1.3.0"
    const val protobuf = "3.17.3"
    const val androidx_test = "1.4.0"
    const val extJUnitVersion = "1.1.3-rc01"
    const val espressoVersion = "3.4.0-rc01"
}
