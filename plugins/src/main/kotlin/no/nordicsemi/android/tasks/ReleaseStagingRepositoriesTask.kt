package no.nordicsemi.android.tasks

import okhttp3.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.time.Duration.Companion.seconds

/**
 * This API endpoint is intended to facilitate similar functionality.
 * As such, it expects the <namespace> value associated with your
 * deployment (e.g. com.example).
 *
 * The endpoint has an optional parameter of publishing_type,
 * which can either be "user_managed", "automatic", or "portal_api".
 *
 * [Documentation](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#post-to-manualuploaddefaultrepositorynamespace)
 * [Try it out](https://ossrh-staging-api.central.sonatype.com/swagger-ui)
 */
@DisableCachingByDefault(
    because = "This task performs a network operation to upload the staging repository, which is not cacheable."
)
open class ReleaseStagingRepositoriesTask : DefaultTask() {

    @Suppress("unused")
    enum class PublishingType(val value: String) {
        /**
         * This type (default) will upload the deployment to the Publisher Portal, where it can be
         * released or dropped by logging in to https://central.sonatype.com/publishing.
         */
        USER_MANAGED("user_managed"),

        /**
         * Automatic publishing will upload the deployment to the Publisher Portal and attempt to
         * automatically release it to Maven Central if it passes validation.
         */
        AUTOMATIC("automatic"),

        /**
         * This type will only upload the deployment to the Publisher Portal.
         *
         * This approach expects that further requests will be made to the Portal Publisher API to
         * get the status of the deployment. The intention of this is to support larger repositories
         * that do not complete validation during the timeout window of a single request.
         */
        PORTAL_API("portal_api")
    }

    init {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
    }

    @TaskAction
    fun releaseStagingRepositories() {
        uploadDefaultRepository(PublishingType.AUTOMATIC)
    }

    @Suppress("SameParameterValue")
    private fun uploadDefaultRepository(publishingType: PublishingType) {
        val service = buildService()
        val response = service
            .uploadDefaultRepository(
                namespace = "no.nordicsemi",
                publishingType = publishingType.value,
            )
            .execute()

        if (!response.isSuccessful) {
            System.err.println("> HTTP ${response.code()}: ${response.errorBody()?.string()}")
            throw HttpException(response)
        }
    }

    private fun buildService(): OssrhStagingApi {
        val username = System.getenv("OSSR_USERNAME")
        val password = System.getenv("OSSR_PASSWORD")

        val credentials = Credentials.bearer(username, password)

        // val logger = object : Logger {
        //     override fun log(message: String) {
        //         if (message.startsWith("Authorization")) {
        //             println("OkHttp: Authorization: Bearer [REDACTED]")
        //         } else {
        //             println("OkHttp: $message")
        //         }
        //     }
        // }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", credentials)
                        .header("Accept", "*/*")
                        .build()
                )
            }
            // only for testing
            // .addInterceptor(
            //     interceptor = HttpLoggingInterceptor(logger)
            //         .apply {
            //             level = HttpLoggingInterceptor.Level.BODY
            //         }
            // )
            .connectTimeout(120.seconds)
            .readTimeout(120.seconds)
            .writeTimeout(120.seconds)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://ossrh-staging-api.central.sonatype.com")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create().asLenient())
            .build()

        return retrofit.create(OssrhStagingApi::class.java)
    }
}
