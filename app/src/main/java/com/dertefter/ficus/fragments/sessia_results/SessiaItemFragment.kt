package com.dertefter.ficus.fragments.sessia_results

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentSessiaItemBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessiaItemFragment : Fragment(R.layout.fragment_sessia_item) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentSessiaItemBinding

    override fun onResume() {
        super.onResume()
        (parentFragment as SessiaResultsFragment).binding?.appBarLayout?.liftOnScrollTargetViewId = binding?.recyclerView?.id!!

    }

    var adapter: SessiaRecyclerViewAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentSessiaItemBinding.bind(view)

        adapter = SessiaRecyclerViewAdapter(this)
        binding.recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            binding.recyclerView.setPadding(0, 0, 0, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        observeSessiaInfo()
    }

    fun observeSessiaInfo(){
        netiCore?.client?.sessiaResultsViewModel?.sessiaResultsLiveData?.observe(viewLifecycleOwner){
            when (it.status){
                Status.LOADING -> {
                    binding.shimmer.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    binding.shimmer.visibility = View.GONE
                    (activity as MainActivity).createNotificationSnackbar("Ошибка загрузки результатов сессии!", "Проверьте подключение к интернету", "error")

                }
                Status.SUCCESS -> {
                    binding.shimmer.visibility = View.GONE
                    CoroutineScope(Dispatchers.Main).launch {
                        val sessiaItems = it.data?.find { it.title == arguments?.getString("title") }?.items
                       adapter?.setItems(sessiaItems)
                    }


                }

            }
        }
    }

}