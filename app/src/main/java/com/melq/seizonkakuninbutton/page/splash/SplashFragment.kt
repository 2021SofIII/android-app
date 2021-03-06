package com.melq.seizonkakuninbutton.page.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.melq.seizonkakuninbutton.MainViewModel
import com.melq.seizonkakuninbutton.R
import com.melq.seizonkakuninbutton.databinding.FragmentSplashBinding

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentSplashBinding? = null
    private val binding: FragmentSplashBinding get() = _binding!!

    private val handler = Handler()

    override fun onStart() {
        super.onStart()

        val pref = activity?.getSharedPreferences("preference_root", Context.MODE_PRIVATE)
        vm.isWatcher = pref?.getBoolean("isWatcher", false) == true

        val runnable = Runnable {
            findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
        }
        handler.postDelayed(
            runnable,
            1300
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this._binding = FragmentSplashBinding.bind(view)

        binding.constraintLayout.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
        }
    }
}