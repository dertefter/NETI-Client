package com.dertefter.ficus.fragments.about

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentAboutBinding
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSearchGroupBinding
import com.dertefter.ficus.databinding.FragmentSearchPersonBinding
import com.dertefter.ficus.databinding.FragmentSettingsBinding
import com.dertefter.ficus.ficus_old.ProfileData
import com.dertefter.ficus.fragments.schedule.ScheduleFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.AuthorizationState
import com.dertefter.neticore.data.Event
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Week
import com.dertefter.neticore.local.AppPreferences
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class AboutFragment : Fragment(R.layout.fragment_about) {

    lateinit var binding: FragmentAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAboutBinding.bind(view)
        val insetTypes =
            WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
        val insets = ViewCompat.getRootWindowInsets(activity?.window!!.decorView)
        val bottom = insets?.getInsets(insetTypes)?.bottom
        binding.scrollView.updatePadding(bottom = bottom!!)
        setupToolbar()
        binding.tg.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nstumobile_dev/"))
            startActivity(browserIntent)
        }


    }
    fun setupToolbar(){
        binding?.appBarLayout?.statusBarForeground = MaterialShapeDrawable.createWithElevationOverlay(context)
        binding?.toolbar?.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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