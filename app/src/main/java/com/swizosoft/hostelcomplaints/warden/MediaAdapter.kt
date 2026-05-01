package com.swizosoft.hostelcomplaints.warden

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swizosoft.hostelcomplaints.databinding.ItemMediaPreviewBinding

class MediaAdapter(private val mediaUrls: List<String>) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMediaPreviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMediaPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = mediaUrls[position]
        Glide.with(holder.binding.mediaImage.context)
            .load(url)
            .centerCrop()
            .into(holder.binding.mediaImage)
    }

    override fun getItemCount() = mediaUrls.size
}
