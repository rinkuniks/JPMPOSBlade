package relanto.jpn.nrf.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import relanto.jpn.nrf.websocket.UnifiedWebSocketService

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {
    
    @Provides
    fun provideUnifiedWebSocketService(
        @ApplicationContext context: Context
    ): UnifiedWebSocketService {
        return UnifiedWebSocketService(context)
    }
}
