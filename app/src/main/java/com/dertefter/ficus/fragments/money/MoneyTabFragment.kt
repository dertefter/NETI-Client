package com.dertefter.ficus.fragments.money

import android.animation.ObjectAnimator
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentMessagesChatListBinding
import com.dertefter.ficus.databinding.FragmentMoneyTabBinding
import com.dertefter.ficus.databinding.FragmentScheduleWeekBinding
import com.dertefter.ficus.fragments.messages.MessagesFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.messages.SenderPerson

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.moneyTextView.updatePadding(bottom = insets.bottom + 8)
            Log.e("bbb", insets.bottom.toString())
            Log.e("ddd", binding.moneyTextView.paddingBottom.toString())
            WindowInsetsCompat.CONSUMED
        }


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