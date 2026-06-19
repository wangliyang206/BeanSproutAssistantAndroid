package com.wly.beansprout.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideLoginDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        // 这里返回我们在DataStoreManager中定义的DataStore实例
        return context.loginDataStore
    }
}