package com.appbin.textrecognitionassignment.ui.text

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.appbin.textrecognitionassignment.R
import com.appbin.textrecognitionassignment.databinding.ShowTextFragmentBinding


class ShowTextFragment : Fragment() {

    private lateinit var viewModel: ShowTextViewModel
    var finalText : String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(ShowTextViewModel::class.java)

        val binding : ShowTextFragmentBinding
            = DataBindingUtil.inflate(inflater,R.layout.show_text_fragment, container, false)

        val argument = ShowTextFragmentArgs.fromBundle(requireArguments())

        val data = argument.imagedata

        val bitmap = data[0].bitmap

        binding.image.setImageBitmap(bitmap)

        val arrayLine = data[0].array

        for (i : Int in 0 until arrayLine.size){

            finalText = finalText + "\n\n" + arrayLine[i]
        }
        binding.text.text = finalText
        return binding.root
    }

}
