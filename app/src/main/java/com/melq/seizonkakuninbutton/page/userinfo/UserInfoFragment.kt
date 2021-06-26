package com.melq.seizonkakuninbutton.page.userinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.melq.seizonkakuninbutton.MainViewModel
import com.melq.seizonkakuninbutton.R
import com.melq.seizonkakuninbutton.databinding.FragmentUserInfoBinding

class UserInfoFragment : Fragment(R.layout.fragment_user_info) {
    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentUserInfoBinding? = null
    private val binding: FragmentUserInfoBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserInfoBinding.bind(view)

        vm.getName()
        vm.name.observe(viewLifecycleOwner) {
            binding.etName.setText(it)
        }
        binding.etEmail.setText(vm.user.email)

        binding.btUpdate.setOnClickListener {
            vm.updateNameClicked(binding.etName.text.toString())
            vm.eMessage.observe(viewLifecycleOwner) {
                if (it != 0) {
                    Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
                    vm.eMessage.value = 0
                }
            }
        }

        binding.btLogout.setOnClickListener {
            vm.logoutClicked()
            vm.done.observe(viewLifecycleOwner) {
                if (it == true) {
                    findNavController().popBackStack()
                    vm.done.value = false
                }
            }
        }

        // FEATURE: パスワード変更機能を付ける
    }
}