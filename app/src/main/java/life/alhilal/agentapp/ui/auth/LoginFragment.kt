package life.alhilal.agentapp.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import life.alhilal.agentapp.data.networks.AuthenticationApi
import life.alhilal.agentapp.data.repository.AuthRepo
import life.alhilal.agentapp.databinding.FragmentLoginBinding
import life.alhilal.agentapp.ui.base.BaseFragment

class LoginFragment : BaseFragment<AuthViewModel,FragmentLoginBinding,AuthRepo>(){


    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater,
        container: ViewGroup?
    )= FragmentLoginBinding.inflate(inflater,container,false)

    override fun getFragmentRepository() = AuthRepo(dataRemoteSource.buildApi(AuthenticationApi::class.java),dataPreferences)


}