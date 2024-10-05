package com.dertefter.ficus.fragments.money

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMoneyTabBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status

class MoneyTabFragment : Fragment(R.layout.fragment_money_tab) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentMoneyTabBinding

    override fun onResume() {
        super.onResume()
        (parentFragment as MoneyFragment).binding?.appBarLayout?.liftOnScrollTargetViewId = binding?.scrollView?.id!!
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentMoneyTabBinding.bind(view)

        val tab = arguments?.getString("tab")
        observeMoney(tab!!)
        netiCore?.client?.moneyViewModel?.loadYearData(tab)

    }

    fun observeMoney(tab: String){
        netiCore?.client?.moneyViewModel?.moneyLiveData?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.moneyTextView.text = ""
                    binding.loading.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    if (it.data?.get(tab) != null){
                        displayHtml(it.data?.get(tab)!!)
                        binding.loading.visibility = View.GONE
                    }else{
                        binding.moneyTextView.text = ""
                        binding.loading.visibility = View.VISIBLE
                    }
                }

                Status.ERROR -> {
                    binding.moneyTextView.text = "Ошибка загрузки"
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    private fun displayHtml(html: String) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, binding.moneyTextView)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        binding.moneyTextView.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        binding.moneyTextView.text = styledText



    }

}