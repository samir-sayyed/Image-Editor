package com.sam.imageeditor.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sam.imageeditor.R
import java.io.File

class ImageAdapter(private val imageList: List<Uri>, private val onClick: (Uri) -> Unit) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imagePath = imageList[position]
        Glide.with(holder.imageView.context)
            .load(imagePath)
            .into(holder.imageView)
        holder.imageView.setOnClickListener {
            onClick(imagePath)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
