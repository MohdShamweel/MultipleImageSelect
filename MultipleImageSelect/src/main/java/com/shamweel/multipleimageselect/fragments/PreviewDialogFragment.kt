package com.shamweel.multipleimageselect.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.shamweel.multipleimageselect.R
import com.shamweel.multipleimageselect.helpers.Constants
import com.shamweel.multipleimageselect.model.ImageData


class PreviewDialogFragment : DialogFragment() {

    private lateinit var imageData: ImageData
    private lateinit var previewImg: ImageView
    private lateinit var txtPrev: TextView
    lateinit var iconPreview: ImageView

    companion object {
        @JvmStatic
        fun newInstance(imageData: ImageData): PreviewDialogFragment {
            var dialog = PreviewDialogFragment()
            dialog.apply {
                arguments = Bundle().apply {
                    putSerializable(Constants.BUNDLE_EXTRA_IMAGES, imageData)
                }
            }
            return dialog
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        onDialogShowAnimation()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageData = it.getSerializable(Constants.BUNDLE_EXTRA_IMAGES) as ImageData
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewImg = view.findViewById(R.id.img_preview)
        iconPreview = view.findViewById(R.id.img_prev_icon)
        txtPrev = view.findViewById(R.id.txt_prev)

        txtPrev.text = imageData.title

        Glide.with(requireActivity())
            .load(imageData.Uri)
            .placeholder(R.drawable.image_placeholder)
            .into(previewImg)

        Glide.with(requireActivity()).load(imageData.Uri)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .apply(RequestOptions.circleCropTransform())
            .into(iconPreview);

    }

    private fun onDialogShowAnimation(){
        var decorView: View? = dialog?.window?.decorView
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            decorView,
            PropertyValuesHolder.ofFloat("scaleX", 0.75f, 1.0f),
            PropertyValuesHolder.ofFloat("scaleY", 0.75f, 1.0f),
            PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f)
        )
        scaleDown.duration = Constants.PREVIEW_DIALOG_WAIT_MILLIS
        scaleDown.start()
    }

}