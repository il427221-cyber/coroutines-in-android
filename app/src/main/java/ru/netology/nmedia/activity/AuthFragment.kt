package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.SignInViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory
import kotlin.getValue


class AuthFragment : Fragment() {

    private val dependencyContainer = DependencyContainer.getInstance()

    //private val viewModel: SignInViewModel by activityViewModels()
    private val viewModel: SignInViewModel by viewModels(
        factoryProducer = {
            ViewModelFactory(dependencyContainer.repository,dependencyContainer.appAuth)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(
            inflater,
            container,
            false
        )

        binding.signIn.setOnClickListener {
            val login = binding.loginInput.text.toString()
            val password = binding.passwordInput.text.toString()
            viewModel.updateUser(login,password)
            findNavController().navigate(R.id.action_authFragment_to_feedFragment)
        }
        return binding.root

    }
}