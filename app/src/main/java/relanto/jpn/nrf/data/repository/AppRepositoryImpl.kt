package relanto.jpn.nrf.data.repository

import javax.inject.Inject

class AppRepositoryImpl @Inject constructor() : AppRepository {
    override suspend fun getAppInfo(): String {
        return "JPMPOS Blade v1.0"
    }
}
