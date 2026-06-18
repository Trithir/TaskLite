import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.util.Properties

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("org.jetbrains.kotlin.plugin.compose")
	id("com.google.devtools.ksp")
}

data class ReleaseSigningConfigInput(
	val storeFile: File,
	val storePassword: String,
	val keyAlias: String,
	val keyPassword: String
)

val releaseSigningInput = loadReleaseSigningConfigInput(rootDir)

android {
	namespace = "io.tasklite"
	compileSdk = 36

	defaultConfig {
		applicationId = "io.tasklite"
		minSdk = 31
		targetSdk = 35
		versionCode = 11
		versionName = "0.1.11"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		vectorDrawables {
			useSupportLibrary = true
		}
	}

	buildTypes {
		val releaseSigningConfig = releaseSigningInput?.let { signingInput ->
			signingConfigs.create("release") {
				storeFile = signingInput.storeFile
				storePassword = signingInput.storePassword
				keyAlias = signingInput.keyAlias
				keyPassword = signingInput.keyPassword
			}
		}

		release {
			signingConfig = releaseSigningConfig
			isMinifyEnabled = true
			isShrinkResources = true
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

	buildFeatures {
		compose = true
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
	}
}

dependencies {
	val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

	implementation("androidx.core:core-ktx:1.18.0")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
	implementation("androidx.activity:activity-compose:1.11.0")
	implementation("androidx.navigation:navigation-compose:2.9.7")
	implementation("androidx.datastore:datastore-preferences:1.2.1")
	implementation("androidx.glance:glance:1.1.1")
	implementation("androidx.glance:glance-appwidget:1.1.1")
	implementation("androidx.glance:glance-material3:1.1.1")
	implementation(composeBom)
	androidTestImplementation(composeBom)
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material3:material3")
	implementation("com.google.android.material:material:1.13.0")
	implementation("androidx.room:room-runtime:2.8.4")
	implementation("androidx.room:room-ktx:2.8.4")
	ksp("androidx.room:room-compiler:2.8.4")

	testImplementation("junit:junit:4.13.2")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
	androidTestImplementation("androidx.test.ext:junit:1.3.0")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
	androidTestImplementation("androidx.compose.ui:ui-test-junit4")
	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")
}

fun loadReleaseSigningConfigInput(rootDir: File): ReleaseSigningConfigInput? {
	val propertiesFile = rootDir.resolve("release-signing.properties")
	val fileProperties = loadPropertiesIfPresent(propertiesFile)
	val storeFilePath = configuredReleaseSigningValue(
		envName = "TASKLITE_RELEASE_STORE_FILE",
		propertyName = "storeFile",
		fileProperties = fileProperties
	)
	val storePassword = configuredReleaseSigningValue(
		envName = "TASKLITE_RELEASE_STORE_PASSWORD",
		propertyName = "storePassword",
		fileProperties = fileProperties
	)
	val keyAlias = configuredReleaseSigningValue(
		envName = "TASKLITE_RELEASE_KEY_ALIAS",
		propertyName = "keyAlias",
		fileProperties = fileProperties
	)
	val keyPassword = configuredReleaseSigningValue(
		envName = "TASKLITE_RELEASE_KEY_PASSWORD",
		propertyName = "keyPassword",
		fileProperties = fileProperties
	)
	val configuredValues = listOf(storeFilePath, storePassword, keyAlias, keyPassword)
	if (configuredValues.all { it.isNullOrBlank() }) {
		return null
	}

	if (configuredValues.any { it.isNullOrBlank() }) {
		error(
			"Release signing is partially configured. Set all TASKLITE_RELEASE_* environment variables or complete release-signing.properties."
		)
	}

	val storeFile = rootDir.resolve(storeFilePath!!)
	if (!storeFile.isFile) {
		error("Release signing storeFile does not exist: $storeFile")
	}

	return ReleaseSigningConfigInput(
		storeFile = storeFile,
		storePassword = storePassword!!,
		keyAlias = keyAlias!!,
		keyPassword = keyPassword!!
	)
}

fun configuredReleaseSigningValue(
	envName: String,
	propertyName: String,
	fileProperties: Properties?
): String? {
	return System.getenv(envName)
		?.takeIf { it.isNotBlank() }
		?: fileProperties?.getProperty(propertyName)?.takeIf { it.isNotBlank() }
}

fun loadPropertiesIfPresent(file: File): Properties? {
	if (!file.isFile) {
		return null
	}

	return Properties().apply {
		file.inputStream().use(::load)
	}
}

tasks.register("printReleaseSigningStatus") {
	group = "help"
	description = "Shows whether TaskLite release signing is configured locally."

	doLast {
		if (releaseSigningInput == null) {
			println("TaskLite release signing is not configured.")
			println("Provide release-signing.properties or all TASKLITE_RELEASE_* environment variables.")
			return@doLast
		}

		println("TaskLite release signing is configured.")
		println("Store file: ${releaseSigningInput.storeFile}")
		println("Key alias: ${releaseSigningInput.keyAlias}")
	}
}
