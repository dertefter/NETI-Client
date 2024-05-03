package com.dertefter.ficus.wearable

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.dertefter.ficus.wearable.databinding.ActivityMainBinding
import com.dertefter.ficus.wearable.schedule.ScheduleFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.schedule.Group
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var binding: ActivityMainBinding
    var netiCore: NETICore? = null

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        netiCore = (application as Ficus).netiCore

        observeGroupInfo()
        netiCore?.checkGroup()
    }

    fun observeGroupInfo(){
        netiCore?.client?.scheduleViewModel?.currentGroupLiveData?.observe(this){
            if (it != null && it.title.isNotEmpty()){
                binding.scheduleLayout.visibility = View.VISIBLE
                binding.helloLayout.visibility = View.GONE
                supportFragmentManager.beginTransaction().replace(R.id.schedule_layout, ScheduleFragment()).commitAllowingStateLoss()

            } else {
                binding.scheduleLayout.visibility = View.GONE
                binding.helloLayout.visibility = View.VISIBLE
                 }
        }
    }


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            // DataItem changed
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    DataMapItem.fromDataItem(item).dataMap.apply {
                        val group_name = getString("group")
                        if (group_name != null) {
                            netiCore?.setGroup(Group(group_name))
                        }
                        Toast.makeText(this@MainActivity, getString("group").toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (event.type == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}