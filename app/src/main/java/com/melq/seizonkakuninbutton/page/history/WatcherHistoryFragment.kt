package com.melq.seizonkakuninbutton.page.history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.melq.seizonkakuninbutton.MainViewModel
import com.melq.seizonkakuninbutton.R
import com.melq.seizonkakuninbutton.databinding.FragmentWatcherHistoryBinding
import com.melq.seizonkakuninbutton.notification.WatcherNotificationReceiver

class WatcherHistoryFragment : Fragment(R.layout.fragment_watcher_history) {
    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentWatcherHistoryBinding? = null
    private val binding: FragmentWatcherHistoryBinding get() = _binding!!

    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            activity?.finish()
        }
    }

    override fun onStart() {
        super.onStart()

        if (!vm.isWatcher) findNavController().popBackStack()
        vm.getUserData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatcherHistoryBinding.bind(view)

        val historyList = vm.user.pushHistory
        adapter = MyAdapter(historyList, requireContext())
        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
            setDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.divider)!!)
        }
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
            it.addItemDecoration(dividerItemDecoration)
        }

        vm.isUserLoaded.observe(viewLifecycleOwner) {
            Log.d("WATCH_HISTORY", "vm:isUserLoaded: $it")
            if (it == true) {
                binding.tvTitle.text = "${vm.user.name} の履歴"
                historyList.run {
                    clear()
                    addAll(vm.user.pushHistory)

                    val notificationManagerCompat = NotificationManagerCompat.from(requireContext())
                    notificationManagerCompat.cancel(R.string.remind_to_watcher)
                    if (this.isNotEmpty()) {
                        val lastTime = last()["timestamp"] as Timestamp
                        WatcherNotificationReceiver.setNotification(requireContext(), lastTime, 24)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.swipeRefreshLayout.isRefreshing = false

                vm.isUserLoaded.value = false
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            vm.getUserData()
        }

        binding.btUserInfo.setOnClickListener {
            findNavController().navigate(R.id.action_watcherHistoryFragment_to_userInfoFragment)
        }
    }
}