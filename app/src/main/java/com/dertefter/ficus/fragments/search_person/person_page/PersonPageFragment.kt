package com.dertefter.ficus.fragments.search_person.person_page

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.dertefter.ficus.Ficus
import com.dertefter.ficus.MainActivity
import com.dertefter.ficus.R
import com.dertefter.ficus.databinding.FragmentPersonPageBinding
import com.dertefter.ficus.databinding.FragmentScheduleBinding
import com.dertefter.ficus.databinding.FragmentSearchGroupBinding
import com.dertefter.ficus.databinding.FragmentSearchPersonBinding
import com.dertefter.ficus.fragments.other.ImageDialogFragment
import com.dertefter.neticore.NETICore
import com.dertefter.neticore.data.Person
import com.dertefter.neticore.data.Status
import com.dertefter.neticore.data.schedule.Group
import com.dertefter.neticore.data.schedule.Week
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso

class PersonPageFragment : Fragment(R.layout.fragment_person_page) {

    var netiCore: NETICore? = null

    lateinit var binding: FragmentPersonPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        netiCore = (activity?.application as Ficus).netiCore
        binding = FragmentPersonPageBinding.bind(view)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val person = PersonPageFragmentArgs.fromBundle(requireArguments()).person

        binding.buttonSite.setOnClickListener {
            val browswerIntent = Intent(Intent.ACTION_VIEW, Uri.parse(person.site))
            startActivity(browswerIntent)
        }
        ObjectAnimator.ofFloat(binding.buttonSite, "scaleX", 0.5f, 1f).apply {
            duration = 300
            start()
        }
        ObjectAnimator.ofFloat(binding.buttonSite, "scaleY", 0.5f, 1f).apply {
            duration = 300
            start()
        }

        if (person.hasTimetable){
            binding.buttonSHedule.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(binding.buttonSHedule, "scaleX", 0.5f, 1f).apply {
                duration = 300
                start()
            }
            ObjectAnimator.ofFloat(binding.buttonSHedule, "scaleY", 0.5f, 1f).apply {
                duration = 300
                start()
            }
            binding.buttonSHedule.setOnClickListener {
                val action = PersonPageFragmentDirections.actionPersonPageFragmentToPersonScheduleFragment(person)
                findNavController().navigate(action)
            }
        }else{
            binding.buttonSHedule.visibility = View.GONE
        }

        if (person.mail.isNullOrEmpty()){
            binding.personMail.visibility = View.GONE
        }else{
            binding.personMail.visibility = View.VISIBLE
            binding.personMail.text = person.mail
        }

        binding.personName.text = person.name

        if (!person.pic.isNullOrEmpty()){
            Picasso.get().load(person.pic).resize(300, 300).centerCrop().into(binding.personAvatarPlaceholder)
            binding.personAvatarPlaceholder.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("image", person.pic)
                val imageDialogFragment = ImageDialogFragment()
                imageDialogFragment.arguments = bundle
                imageDialogFragment.show(parentFragmentManager, "image")
            }
        }
    }



}