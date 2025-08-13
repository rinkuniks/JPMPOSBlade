package relanto.jpn.nrf.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Event> : ViewModel() {
    
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<State> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    abstract fun createInitialState(): State
    
    protected fun setState(reduce: State.() -> State) {
        val newState = uiState.value.reduce()
        _uiState.value = newState
    }
    
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    protected fun setError(errorMessage: String?) {
        _error.value = errorMessage
    }
    
    protected fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                setLoading(true)
                setError(null)
                block()
            } catch (e: Exception) {
                setError(e.message ?: "An error occurred")
            } finally {
                setLoading(false)
            }
        }
    }
    
    abstract fun onEvent(event: Event)
}
