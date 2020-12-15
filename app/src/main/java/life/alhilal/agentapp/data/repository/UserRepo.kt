package life.alhilal.agentapp.data.repository

import life.alhilal.agentapp.data.networks.UserApi

class UserRepo(
    private val api: UserApi
) : BaseRepo() {

    suspend fun getUser() = safeApiCall {
        api.getUser()
    }

}