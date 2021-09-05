package com.shamweel.multipleimageselect.adapter

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shamweel.multipleimageselect.R
import com.shamweel.multipleimageselect.helpers.GlobalVariables
import com.shamweel.multipleimageselect.helpers.ImageHelper
import com.shamweel.multipleimageselect.model.ImageData

class ImageAdapter(
    var context: Context,
    var images: ArrayList<ImageData>,
    var listener: ImageListener
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    var gridSize = 200
    private lateinit var tName: String

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imageView: ImageView
        lateinit var imageViewBack: ImageView
        lateinit var view: View
        lateinit var consItem: ConstraintLayout
        lateinit var consItemBack: ConstraintLayout
        lateinit var consPlaceholder: ConstraintLayout
        lateinit var txtSelectedPos: TextView

        lateinit var consPlaceHolderView: View

        fun bindItems(imageData: ImageData) {

            imageView = itemView.findViewById(R.id.image_view_image_select)
            imageViewBack = itemView.findViewById(R.id.image_view_image_select_back)
            consItem = itemView.findViewById(R.id.consItem)
            consItemBack = itemView.findViewById(R.id.consItemBack)
            consPlaceholder = itemView.findViewById(R.id.cons_placeholder)
            txtSelectedPos = itemView.findViewById(R.id.txt_selected_pos)
            consPlaceHolderView = itemView.findViewById(R.id.cons_placeholder_view)

            consPlaceholder.layoutParams.width = gridSize

            if (ImageHelper.getSelectedIndex(imageData) != -1) {
                consItemBack.visibility = View.VISIBLE
                consItemBack.alpha = 1f
                consItem.visibility = View.GONE
                consItemBack.alpha = 1f
                txtSelectedPos.text = ImageHelper.getSelectedIndex(imageData).toString()
            } else {
                consItem.visibility = View.VISIBLE
                consItem.alpha = 1f
                consItemBack.visibility = View.GONE
                consItemBack.alpha = 1f
            }

            consPlaceHolderView.setOnClickListener {
                if (ImageHelper.getSelectedIndex(imageData) == -1) {
                    if (ImageHelper.arrayList.size >= GlobalVariables.currentLimit) {
                        Toast.makeText(
                            context,
                            "Can't share more than ${GlobalVariables.currentLimit} images.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setOnClickListener
                    }

                    flipCard(context, consItemBack, consItem)
                    ImageHelper.addToSelectedList(imageData)
                    txtSelectedPos.text = ImageHelper.getSelectedIndex(imageData).toString()
                    listener.onImageSelectedListChange(imageData, false)
                } else {
                    flipCard(context, consItem, consItemBack)
                    ImageHelper.removeFromSelectedList(imageData)
                    listener.onImageSelectedListChange(imageData, true)
                }
            }

            consPlaceHolderView.setOnLongClickListener {
                listener.onImageLongClick(it, imageData)
                return@setOnLongClickListener true
            }

            loadGlideImages(imageView, imageData.Uri)
            loadGlideImages(imageViewBack, imageData.Uri)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(images[position])
    }

    override fun getItemCount(): Int = images.size


    fun setLayoutParams(size: Int) {
        this.gridSize = size
    }

    interface ImageListener {
        fun onImageLongClick(view: View, imageData: ImageData)
        fun onImageSelectedListChange(imageData: ImageData, remove: Boolean)
    }

    private fun loadGlideImages(imageView: ImageView, uri: String) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.drawable.image_placeholder)
            .into(imageView)
    }


    fun flipCard(
        context: Context,
        visibleView: View,
        inVisibleView: View,
    ) {
        try {
            visibleView.visibility = View.VISIBLE
            inVisibleView.visibility = View.GONE

            val scale = context.resources.displayMetrics.density
            val cameraDist = 8000 * scale
            visibleView.cameraDistance = cameraDist
            inVisibleView.cameraDistance = cameraDist

            val flipOutAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.flip_out
                ) as AnimatorSet
            flipOutAnimatorSet.setTarget(inVisibleView)

            val flipInAnimatorSet =
                AnimatorInflater.loadAnimator(
                    context,
                    R.animator.flip_in
                ) as AnimatorSet

            flipInAnimatorSet.setTarget(visibleView)
            flipOutAnimatorSet.start()
            flipInAnimatorSet.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
