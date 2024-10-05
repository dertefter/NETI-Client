package com.dertefter.ficus.fragments.news

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentReadNewsBinding
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors
import com.smarttoolfactory.extendedcolors.md3.hct.Cam16
import com.smarttoolfactory.extendedcolors.md3.palettes.TonalPalette
import com.smarttoolfactory.extendedcolors.util.material3ToneRange
import com.squareup.picasso.Picasso
import kotlin.math.max


class ReadNewsFragment : Fragment(R.layout.fragment_read_news) {
    var netiCore: NETICore? = null

    var colorPrimary: Color? = null
    var colorOnPrimary: Color? = null
    var colorSurface: Color? = null
    var colorOnSurface: Color? = null
    var colorPrimaryContainer: Color? = null
    var colorOnPrimaryContainer: Color? = null
    var colorSurfaceContainer: Color? = null
    var colorOnSurfaceContainer: Color? = null


    lateinit var binding: FragmentReadNewsBinding

    var adapter: NewsListAdapter? = null

    fun getColorTonesMap(color: Color): Map<Int, Color> {
        val palette: TonalPalette = TonalPalette.fromInt(color.toArgb())
        val toneMap = linkedMapOf<Int, Color>()
        for (i in 0..100){
            val p = palette.tone(i)
            toneMap[i] = Color.valueOf(p)
        }

        return toneMap
    }
    fun setupColors(){
        if (colorPrimary != null){
            binding.shareButton.setBackgroundColor(colorPrimary!!.toArgb())
            binding.progressBar.setIndicatorColor(colorPrimary!!.toArgb())
            binding.title.setTextColor(colorPrimary!!.toArgb())
            binding.newsContentView.setLinkTextColor(colorPrimary!!.toArgb())
            binding.newsContactsView.setLinkTextColor(colorPrimary!!.toArgb())
            binding.tt.setLinkTextColor(colorPrimary!!.toArgb())
        }
        if (colorOnPrimary != null){
            binding.shareButton.setTextColor(colorOnPrimary!!.toArgb())

        }
        if (colorPrimaryContainer != null){
            binding.scrollUpFab.backgroundTintList = ColorStateList.valueOf(colorPrimaryContainer!!.toArgb())
            binding.backButton.setBackgroundColor(colorPrimaryContainer!!.toArgb())
        }
        if (colorSurface != null){
            binding.contactsCard.setCardBackgroundColor(colorSurface!!.toArgb())
        }
        if (colorOnSurface != null){
            binding.scrollUpFab.imageTintList = ColorStateList.valueOf(colorOnSurface!!.toArgb())
            binding.newsContentView.setTextColor(colorOnSurface!!.toArgb())
            binding.newsContactsView.setTextColor(colorOnSurface!!.toArgb())
        }
        if (colorSurfaceContainer != null){
            binding.root.setBackgroundColor(colorSurfaceContainer!!.toArgb())
            binding.appBarLayout.setBackgroundColor(colorSurfaceContainer!!.toArgb())
            binding.collapsingAppBar.setContentScrimColor(colorSurfaceContainer!!.toArgb())
            binding.title.setBackgroundColor(colorSurfaceContainer!!.toArgb())
            binding.collapsingAppBar.setBackgroundColor(colorSurfaceContainer!!.toArgb())
            binding.nestedScrollView.setBackgroundColor(colorSurfaceContainer!!.toArgb())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       if (AppPreferences.monet_news == false){
           return layoutInflater.inflate(R.layout.fragment_read_news, container, false)
       }
        var color = ReadNewsFragmentArgs.fromBundle(requireArguments()).color
        if (DynamicColors.isDynamicColorAvailable()){
            val context: Context = DynamicColors.wrapContextIfAvailable(
                requireContext(),
                DynamicColorsOptions.Builder()
                    .setContentBasedSource(color)
                    .build()
            )
            return layoutInflater.cloneInContext(context).inflate(R.layout.fragment_read_news, container, false)
        } else {
            val context: Context = DynamicColors.wrapContextIfAvailable(
                requireContext(),
                DynamicColorsOptions.Builder()
                    .setContentBasedSource(color)
                    .build()
            )
            val cm = getColorTonesMap(Color.valueOf(color))
            when (context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    colorPrimary = cm[90]
                    colorOnPrimary = cm[10]
                    colorSurface = cm[0]
                    colorOnSurface = cm[99]
                    colorPrimaryContainer = cm[20]
                    colorOnPrimaryContainer = cm[95]
                    colorSurfaceContainer = cm[3]
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    colorPrimary = cm[40]
                    colorOnPrimary = cm[95]
                    colorSurface = cm[100]
                    colorOnSurface = cm[0]
                    colorPrimaryContainer = cm[90]
                    colorOnPrimaryContainer = cm[10]
                    colorSurfaceContainer = cm[95]
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }
            return layoutInflater.cloneInContext(context).inflate(R.layout.fragment_read_news, container, false)

        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore

        binding = FragmentReadNewsBinding.bind(view)
///////////////////////
        setupColors()
///////////////////////
        binding.title.text = arguments?.getString("title")
        if (arguments?.getString("imageUrl") != null){
            binding.backgroundNews.visibility = View.VISIBLE
            Picasso.get().load(arguments?.getString("imageUrl")).into(binding.backgroundNews)
        }else{
            binding.backgroundNews.visibility = View.GONE
        }
        val newsid = arguments?.getString("newsid")

        observeNewsContent()

        setupAppBarButtons()
        setupFab()

        netiCore?.client?.newsViewModel?.readNews(newsid!!)

    }


    fun setupFab(){
        binding.scrollUpFab.hide()
        binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > 0){
                binding.scrollUpFab.show()
            }else{
                binding.scrollUpFab.hide()
            }
        }
        binding.scrollUpFab.setOnClickListener {
            binding?.nestedScrollView?.smoothScrollTo(0,0)
            binding?.appBarLayout?.setExpanded(true)
        }
    }

    fun setupAppBarButtons(){
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.shareButton.setOnClickListener {
            val url = "https://nstu.ru/news/news_more?idnews=" + arguments?.getString("newsid")!!
            val i = Intent(Intent.ACTION_SEND)
            i.setType("text/plain")
            i.putExtra(Intent.EXTRA_SUBJECT, "Поделиться")
            i.putExtra(Intent.EXTRA_TEXT, url)
            startActivity(Intent.createChooser(i, "Поделиться"))
        }
    }

    fun observeNewsContent(){
        netiCore?.client?.newsViewModel?.newsContentLiveData?.observe(viewLifecycleOwner) {
            when (it.status){
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.newsContentView.visibility = View.VISIBLE
                    binding.errorView.visibility = View.GONE
                    binding.contactsCard.visibility = View.VISIBLE
                    displayHtml(it.data?.htmlBody!!, binding.newsContentView)
                    displayHtml(it.data?.htmlContacts!!, binding.newsContactsView)
                    ObjectAnimator.ofFloat(binding.newsContentView, "alpha", 0f, 1f).apply {
                        duration = 250
                        start()
                    }
                    ObjectAnimator.ofFloat(binding.contactsCard, "alpha", 0f, 1f).apply {
                        duration = 250
                        start()
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.newsContentView.visibility = View.GONE
                    binding.contactsCard.visibility = View.GONE
                    binding.errorView.visibility = View.VISIBLE
                    binding.retryButton.setOnClickListener {
                        netiCore?.client?.newsViewModel?.readNews(arguments?.getString("newsid")!!)
                    }

                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.newsContentView.visibility = View.GONE
                    binding.contactsCard.visibility = View.GONE
                    binding.errorView.visibility = View.GONE
                }
            }
        }
    }


    private fun displayHtml(html: String, textView: TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, textView)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_COMPACT,
            imageGetter,null)

        // to enable image/link clicking
        textView.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        textView.text = styledText
    }



}