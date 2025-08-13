package relanto.jpn.nrf.data.repository

interface AppRepository {
    suspend fun getAppInfo(): String
}
