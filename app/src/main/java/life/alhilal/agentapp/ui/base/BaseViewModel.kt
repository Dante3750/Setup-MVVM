package life.alhilal.agentapp.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import life.alhilal.agentapp.data.networks.UserApi
import life.alhilal.agentapp.data.repository.BaseRepo

abstract class BaseViewModel(
    private val repository: BaseRepo
) : ViewModel() {

    suspend fun logout(api: UserApi) = withContext(Dispatchers.IO) { repository.logout(api) }

}