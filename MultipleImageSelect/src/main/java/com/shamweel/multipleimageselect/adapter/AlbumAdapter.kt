package com.shamweel.multipleimageselect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shamweel.multipleimageselect.R
import com.shamweel.multipleimageselect.model.AlbumData


class AlbumAdapter(
    var context: Context,
    var arrayList: ArrayList<AlbumData>,
    var listener: AlbumListener
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    var gridSize = 200;


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(albumData: AlbumData) {
            itemView.findViewById<TextView>(R.id.text_view_album_name).text = albumData.title

            ViewCompat.setTransitionName(
                itemView.findViewById<View>(R.id.cos_item),
                context.resources.getString(R.string.simple_transition) + adapterPosition
            )
            itemView.findViewById<View>(R.id.view_album).setOnClickListener {
                listener.onAlbumClick(albumData, itemView.findViewById<View>(R.id.cos_item))
            }

            Glide
                .with(itemView.context)
                .load(albumData.uri)
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .into(itemView.findViewById(R.id.image_view_album_image))

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun setLayoutParams(size: Int) {
        this.gridSize = size
    }

    interface AlbumListener {
        fun onAlbumClick(albumData: AlbumData, view: View)
    }


}