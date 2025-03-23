package app.echoirx.di

import android.content.Context
import app.echoirx.data.remote.api.GithubApiService
import app.echoirx.data.update.UpdateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    @Singleton
    fun provideUpdateManager(
        @ApplicationContext context: Context,
        githubApiService: GithubApiService
    ): UpdateManager = UpdateManager(githubApiService, context)
}