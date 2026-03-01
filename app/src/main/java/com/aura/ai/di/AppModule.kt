package com.aura.ai.di

import android.content.Context
import com.aura.ai.core.chat.ChatEngine
import com.aura.ai.core.model.Qwen3Model
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideQwen3Model(@ApplicationContext context: Context): Qwen3Model {
        return Qwen3Model(context)
    }
    
    @Provides
    @Singleton
    fun provideChatEngine(
        @ApplicationContext context: Context,
        qwen3Model: Qwen3Model
    ): ChatEngine {
        return ChatEngine(context, qwen3Model)
    }
}