package com.melq.seizonkakuninbutton

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.melq.seizonkakuninbutton.databinding.FragmentMainBinding
import com.melq.seizonkakuninbutton.notification.NotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {
    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finish()
        }
    }

    override fun onStart() {
        super.onStart()

        Log.d("MAIN_FRAGMENT", "isWatcher: ${vm.isWatcher}, " +
                "pref.isWatcher: ${activity?.getSharedPreferences("preference_root", Context.MODE_PRIVATE)?.getBoolean("isWatcher", false)}")

        val user = vm.auth.currentUser // FEATURE: ここの処理はActivityの初期化処理内に移したい
        if (user != null) {
            Log.d("MAIN_FRAGMENT", "email: ${user.email}, uid: ${user.uid}")

            if (vm.isWatcher) findNavController().navigate(R.id.action_mainFragment_to_watcherHistoryFragment)
            else vm.getUserData()
        } else {
            Log.d("MAIN_FRAGMENT", "no userdata")
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        binding.btMain.setOnClickListener {
            if (vm.canPush) {
                vm.canPush = false
                GlobalScope.launch(Dispatchers.Main) {
                    for (i in 109 downTo 10) {
                        binding.btMain.background =
                            AppCompatResources.getDrawable(requireContext(), R.drawable.circle_gray)
                        binding.tvBtText.text = (i / 10).toString()
                        delay(100)
                    }
                    vm.canPush = true
                    binding.btMain.background =
                        AppCompatResources.getDrawable(requireContext(), R.drawable.ripple_circle)
                    binding.tvBtText.text = getString(R.string.push)
                }

                vm.buttonPushed()
                vm.done.observe(viewLifecycleOwner) {
                    if (it == true) {
                        Snackbar.make(view, R.string.button_pushed, Snackbar.LENGTH_SHORT).show()
                        vm.done.value = false
                    }
                }

                // 通知関連. context含むのでVMに渡せない
                val notificationManagerCompat = NotificationManagerCompat.from(requireContext())
                notificationManagerCompat.cancel(R.string.app_name)
                NotificationReceiver.setNotification(context)
            }
        }

        binding.btHistory.setOnClickListener {
            if (vm.isUserLoaded.value == true)
                findNavController().navigate(R.id.action_mainFragment_to_historyFragment)
        }

        binding.btUserInfo.setOnClickListener{
            if (vm.isUserLoaded.value == true) {
                if (vm.auth.currentUser != null) {
                    findNavController().navigate(R.id.action_mainFragment_to_userInfoFragment)
                } else
                    findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
            }
        }
    }
}