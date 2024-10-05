package com.dertefter.ficus.fragments.search_person.person_page

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.ImageGetter
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentPersonPageBinding
import com.dertefter.ficus.fragments.other.ImageDialogFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.squareup.picasso.Picasso

class PersonPageFragment : Fragment(R.layout.fragment_person_page) {

    var netiCore: NETICore? = null
    lateinit var binding: FragmentPersonPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide)
    }

    private fun displayHtml(html: String, tv: TextView) {

        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, tv)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH,
            imageGetter,null)

        // to enable image/link clicking
        tv.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        tv.text = styledText



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentPersonPageBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val personLink: String? = PersonPageFragmentArgs.fromBundle(requireArguments()).personLink
        val personName: String? = PersonPageFragmentArgs.fromBundle(requireArguments()).personName
        ObjectAnimator.ofFloat(binding.buttonSite, "scaleX", 0.5f, 1f).apply {
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(binding.buttonSite, "scaleY", 0.5f, 1f).apply {
            duration = 300
            start()
        }
        val liveData = MutableLiveData<Person?>()
        if (!personLink.isNullOrEmpty()){
            netiCore!!.personHelper!!.retrivePerson(personLink, liveData)
        }
        else{
            if (!personName.isNullOrEmpty()){
                netiCore!!.personHelper!!.retrivePersonByName(personName, liveData)
            }
        }

        liveData.observe(viewLifecycleOwner) {
            if (it != null){
                ObjectAnimator.ofFloat(binding.base, "alpha", 1f).start()
                binding.shimmer.visibility = View.GONE
                binding.base.visibility = View.VISIBLE

                val p = it
                binding.personName.text = it.name
                binding.appBarLayout.addLiftOnScrollListener { fl, i ->
                    if(binding.appBarLayout.isLifted){
                        binding.toolbar.title = it.shortName
                    }else{
                        binding.toolbar.title = ""
                    }
                }

                binding.personPost.text = it.personPost

                if (it.mail.isNullOrEmpty() && it.contacts__card_address.isNullOrEmpty() && it.contacts__card_time.isNullOrEmpty()){
                    binding.contactsCard.visibility = View.GONE
                }

                (binding.mail.parent as View).visibility = if (it.mail.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.mail.text = it.mail ?: ""

                (binding.address.parent as View).visibility = if (it.contacts__card_address.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.address.text = it.contacts__card_address ?: ""

                (binding.time.parent as View).visibility = if (it.contacts__card_time.isNullOrEmpty()) View.GONE else View.VISIBLE
                binding.time.text = it.contacts__card_time ?: ""

                if (!it.about_disc.isNullOrEmpty()){
                    displayHtml(it.about_disc!!, binding.aboutDisc)
                }else{
                    (binding.aboutDisc.parent as View).visibility = View.GONE
                }

                if (!it.personProfiles.isNullOrEmpty()){
                    displayHtml(it.personProfiles!!, binding.profiles)
                }else{
                    (binding.profiles.parent as View).visibility = View.GONE
                }

                val site = it.site
                if (site.isNullOrEmpty()){
                    binding.buttonSite.isEnabled = false
                } else {
                    binding.buttonSite.isEnabled = true
                    binding.buttonSite.setOnClickListener {
                        val browswerIntent = Intent(Intent.ACTION_VIEW, Uri.parse(site))
                        startActivity(browswerIntent)
                    }
                }
                if (!p.pic.isNullOrEmpty()){
                    Picasso.get().load(p.pic).resize(300, 300).centerCrop().into(binding.personAvatarPlaceholder)
                    binding.personAvatarPlaceholder.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("image", p.pic)
                        val imageDialogFragment = ImageDialogFragment()
                        imageDialogFragment.arguments = bundle
                        imageDialogFragment.show(parentFragmentManager, "image")
                    }
                }

                if (it.hasTimetable){
                    binding.buttonSHedule.visibility = View.VISIBLE
                    binding.buttonSHedule.setOnClickListener {
                        val action = PersonPageFragmentDirections.actionPersonPageFragmentToPersonScheduleFragment(p)
                        findNavController().navigate(action)
                    }
                }else{
                    binding.buttonSHedule.visibility = View.GONE
                }


            }
            else{
                binding.shimmer.visibility = View.VISIBLE
                binding.base.visibility = View.GONE
            }
        }



}
}