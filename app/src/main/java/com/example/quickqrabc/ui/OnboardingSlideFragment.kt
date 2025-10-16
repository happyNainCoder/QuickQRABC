package com.example.quickqrabc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quickqrabc.databinding.OnboardingSlideBinding

class OnboardingSlideFragment : Fragment() {
    
    private var _binding: OnboardingSlideBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val ARG_SLIDE_DATA = "slide_data"
        
        fun newInstance(slideData: OnboardingActivity.OnboardingSlide): OnboardingSlideFragment {
            val fragment = OnboardingSlideFragment()
            val args = Bundle()
            args.putSerializable(ARG_SLIDE_DATA, slideData)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OnboardingSlideBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val slideData = arguments?.getSerializable(ARG_SLIDE_DATA) as? OnboardingActivity.OnboardingSlide
        slideData?.let { setupSlide(it) }
    }
    
    private fun setupSlide(slideData: OnboardingActivity.OnboardingSlide) {
        binding.imageIllustration.setImageResource(slideData.image)
        binding.textTitle.text = slideData.title
        binding.textDescription.text = slideData.description
        
        if (slideData.isPermissionSlide) {
            binding.permissionCard.visibility = View.VISIBLE
            binding.textPermissionTitle.text = slideData.permissionTitle
            binding.textPermissionDescription.text = slideData.permissionDescription
        } else {
            binding.permissionCard.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
