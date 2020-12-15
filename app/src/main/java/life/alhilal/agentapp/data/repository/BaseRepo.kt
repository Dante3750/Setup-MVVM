package life.alhilal.agentapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import life.alhilal.agentapp.data.networks.NetworkResource
import life.alhilal.agentapp.data.networks.UserApi
import retrofit2.HttpException

abstract class BaseRepo {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResource<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        NetworkResource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        NetworkResource.Failure(true, null, null)
                    }
                }
            }
        }
    }

    suspend fun logout(api: UserApi) = safeApiCall {
        api.logout()
    }
}