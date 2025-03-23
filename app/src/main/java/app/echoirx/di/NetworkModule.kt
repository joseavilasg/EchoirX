package app.echoirx.di

import app.echoirx.data.remote.api.ApiService
import app.echoirx.data.remote.api.GithubApiService
import app.echoirx.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = Duration.ofMinutes(2).toMillis()
            connectTimeoutMillis = Duration.ofSeconds(20).toMillis()
            socketTimeoutMillis = Duration.ofMinutes(1).toMillis()
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            retryOnException(maxRetries = 3, retryOnTimeout = true)
            exponentialDelay()
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        engine {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(Duration.ofSeconds(20))
                readTimeout(Duration.ofMinutes(1))
                writeTimeout(Duration.ofMinutes(1))
            }
        }
    }

    @Provides
    @Singleton
    fun provideApiService(
        client: HttpClient,
        settingsRepository: SettingsRepository
    ): ApiService = ApiService(client, settingsRepository)

    @Provides
    @Singleton
    fun provideGithubApiService(
        client: HttpClient
    ): GithubApiService = GithubApiService(client)
}