package life.alhilal.agentapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import life.alhilal.agentapp.data.DataPreferences
import life.alhilal.agentapp.data.networks.DataRemoteSource
import life.alhilal.agentapp.data.networks.UserApi
import life.alhilal.agentapp.data.repository.BaseRepo
import life.alhilal.agentapp.ui.auth.AuthenticationActivity
import life.alhilal.agentapp.ui.startNewActivity

abstract class BaseFragment<VM : BaseViewModel, B : ViewBinding, R : BaseRepo> : Fragment() {

    protected lateinit var dataPreferences: DataPreferences
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    protected val dataRemoteSource = DataRemoteSource()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataPreferences = DataPreferences(requireContext())
        binding = getFragmentBinding(inflater, container)
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory).get(getViewModel())

        lifecycleScope.launch { dataPreferences.authToken.first() }

        return binding.root
    }

    fun logout() = lifecycleScope.launch{
        val authToken = dataPreferences.authToken.first()
        val api = dataRemoteSource.buildApi(UserApi::class.java, authToken)
        viewModel.logout(api)
        dataPreferences.clear()
        requireActivity().startNewActivity(AuthenticationActivity::class.java)
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract fun getFragmentRepository(): R

}