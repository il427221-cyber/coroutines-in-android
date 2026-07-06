package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import kotlin.getValue

class SignUpFragment: Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(
            inflater,
            container,
            false
        )
        binding.login.text.toString()

        binding.signUp.setOnClickListener {
            val name = binding.studentName.text.toString()
            val login = binding.studentLogin.text.toString()
            val password = binding.studentPassword.text.toString()
           viewModel.registerUser(login,password,name)
            findNavController().navigate(R.id.action_signUpFragment_to_feedFragment)
        }
        return binding.root

    }
}