package io.iskopasi.githubobserverclient.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.iskopasi.githubobserverclient.repo.Repo
import io.iskopasi.githubobserverclient.repo.Rest
import io.iskopasi.githubobserverclient.repo.getRetrofit
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention
@Qualifier
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
class HiltModules {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineContext = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineContext = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineContext = Dispatchers.Main

    @Provides
    @Singleton
    fun getRepo(@ApplicationContext context: Context): Repo = Repo(getRest(context))

    @Provides
    @Singleton
    fun getRest(@ApplicationContext context: Context): Rest =
        getRetrofit(context).create(Rest::class.java)
}