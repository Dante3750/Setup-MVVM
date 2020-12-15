package life.alhilal.agentapp.data.repository

import life.alhilal.agentapp.data.DataPreferences
import life.alhilal.agentapp.data.networks.AuthenticationApi

class AuthRepo(
    private val api: AuthenticationApi,
    private val preferences: DataPreferences
) : BaseRepo(){

    suspend fun login(
        email: String,
        password: String,
        type : String
    ) = safeApiCall {
        api.login(email,password,type)
    }

    suspend fun saveAuthToken(token: String){
        preferences.saveAuthToken(token)
    }

}