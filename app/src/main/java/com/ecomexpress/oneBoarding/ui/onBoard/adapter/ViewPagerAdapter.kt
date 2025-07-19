package com.ecomexpress.oneBoarding.ui.onBoard.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.onBoarding.BannerData

class ViewPagerAdapter(val context: Context, private val images: List<BannerData>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivImage)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewPagerAdapter.ViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.include_img_item, parent, false)
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.ViewPagerViewHolder, position: Int) {
        Glide
            .with(context)
            .load(images[position].banner_path)
            .placeholder(R.drawable.placeholder)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(holder.imageView)

    }


    override fun getItemCount(): Int {
        return images.size
    }
}