package life.alhilal.agentapp.data.networks

import life.alhilal.agentapp.data.responses.LoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthenticationApi {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("type" ) type : String
    ) : LoginResponse
}