package com.dertefter.ficus.fragments.news

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentNewsBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.google.android.material.color.MaterialColors


class NewsFragment : Fragment(R.layout.fragment_news) {
    var netiCore: NETICore? = null

    lateinit var binding: FragmentNewsBinding

    var adapter: NewsListAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentNewsBinding.bind(view)

        binding.newsTopAppBarLayout.setStatusBarForegroundColor(MaterialColors.getColor(binding.newsTopAppBarLayout, com.google.android.material.R.attr.colorSurface))

        observeNews()

        setupRecyclerView()

        setupFab()

        getNews()
        binding.swiperefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getNews(true)
        })
    }

    fun setupFab(){
        binding.scrollUpFab.hide()
        binding.scrollUpFab.setOnClickListener {
            getNews(true)
            binding.newsRecyclerView.smoothScrollToPosition(0)
            binding.newsTopAppBarLayout.setExpanded(true)
            binding.scrollUpFab.hide()
        }
    }

    fun setupRecyclerView(){
        adapter = NewsListAdapter(this)
        binding.newsRecyclerView.adapter = adapter
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.newsRecyclerView.layoutManager = GridLayoutManager(context, 2)
            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
            val itemDecorator2 = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
            itemDecorator2.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_horizontal)!!)
            binding.newsRecyclerView.addItemDecoration(itemDecorator)
            binding.newsRecyclerView.addItemDecoration(itemDecorator2)
        }else{
            binding.newsRecyclerView.layoutManager = LinearLayoutManager(context)
            val itemDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
            binding.newsRecyclerView.addItemDecoration(itemDecorator)
        }



        binding.newsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val layoutManager = binding.newsRecyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItemPosition + 1 <= (adapter!!.getLastPosition())) {
                    getNews()
                }
                if (lastVisibleItemPosition >= 12){
                    if (lastVisibleItemPosition >= 28){
                        binding.scrollUpFab.shrink()
                    }else{
                        binding.scrollUpFab.extend()
                    }
                    binding.scrollUpFab.show()
                }else{
                    binding.scrollUpFab.hide()
                }

            }
        })
    }

    fun observeNews(){
        netiCore?.client?.newsViewModel?.newsListLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {binding.swiperefresh.isRefreshing = true}
                Status.SUCCESS -> {
                    adapter?.setNewsList(it.data)
                    binding.swiperefresh.isRefreshing = false
                }
                Status.ERROR -> {binding.swiperefresh.isRefreshing = false}
            }
        }
    }

    fun getDominantColor(bm: Bitmap): Int {
        return Palette.from(bm).generate().getVibrantColor(
            Color.GRAY
        )

    }

    fun readNews(itemView: View, title: String, newsid: String, date: String, imageUrl: String?, color: Int) {
        val action = NewsFragmentDirections.actionNewsFragmentToReadNewsFragment(title, newsid, date, imageUrl, color)
        findNavController().navigate(action)

    }

    fun getNews(isNeedClear: Boolean = false) {
        if (isNeedClear){
           binding.newsRecyclerView.scrollToPosition(0)
        }
        netiCore?.client?.newsViewModel?.getNewsList(isNeedClear)
    }

}