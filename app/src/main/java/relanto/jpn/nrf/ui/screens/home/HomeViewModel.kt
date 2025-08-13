package relanto.jpn.nrf.ui.screens.home

import dagger.hilt.android.lifecycle.HiltViewModel
import relanto.jpn.nrf.base.BaseViewModel
import relanto.jpn.nrf.data.repository.AppRepository
import relanto.jpn.nrf.websocket.UnifiedWebSocketService
import javax.inject.Inject

data class HomeState(
    val title: String = "JPMPOS Blade",
    val subtitle: String = "Your POS solution"
)

sealed class HomeEvent {
    object Refresh : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val unifiedWebSocketService: UnifiedWebSocketService
) : BaseViewModel<HomeState, HomeEvent>() {
    
    override fun createInitialState(): HomeState = HomeState()
    
    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Refresh -> {
                refreshAppInfo()
            }
        }
    }
    
    private fun refreshAppInfo() {
        launchCoroutine {
            val appInfo = appRepository.getAppInfo()
            setState {
                copy(subtitle = appInfo)
            }
        }
    }
    
    // Expose WebSocket service states
    val connectionState = unifiedWebSocketService.clientState
    val isConnected = unifiedWebSocketService.isClientConnected()
}
