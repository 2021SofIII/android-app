package com.melq.seizonkakuninbutton.page.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.melq.seizonkakuninbutton.R
import java.util.*

class MyAdapter(private val historyList: MutableList<Timestamp>, private val context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val box: View = view.findViewById(R.id.box)
        val tvPushedTime: TextView = view.findViewById(R.id.tv_pushed_time)
        val tvComment: TextView = view.findViewById(R.id.tv_comment)
        val tvHoursAgo: TextView = view.findViewById(R.id.tv_hours_ago)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historyData = historyList[historyList.size - 1 - position]
        val timestamp = historyData["timestamp"] as Timestamp
        val comment = historyData["comment"] as String

        holder.run {
            val calendar = Calendar.getInstance()
            calendar.time = historyData.toDate()
            val pushedTimeStr = "${calendar.get(Calendar.MONTH) + 1}月" +
                        "${calendar.get(Calendar.DATE)}日 " +
                        "${calendar.get(Calendar.HOUR_OF_DAY)}:" +
                        "${calendar.get(Calendar.MINUTE)}:" +
                        "${calendar.get(Calendar.SECOND)}"
            tvPushedTime.text = pushedTimeStr

            tvComment.text = comment

            val now = Calendar.getInstance().apply { time = Date() }
            val diffTime = now.timeInMillis - calendar.timeInMillis
            val millisOfHours = 1000 * 60 * 60
            val diffHours = diffTime / millisOfHours
            tvHoursAgo.text = "$diffHours 時間前"
            if (position == 0) {
                box.setBackgroundColor(context.resources.getColor(R.color.white, null))
                if (diffHours >= 24) tvHoursAgo.setTextColor(context.resources.getColor(R.color.red, null))
                else tvHoursAgo.setTextColor(context.resources.getColor(R.color.green, null))
            }
        }
    }

    override fun getItemCount(): Int = historyList.size
}