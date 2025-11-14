package com.justyn.travelmap.profile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.justyn.travelmap.R;
import com.justyn.travelmap.model.CartItem;
import com.justyn.travelmap.model.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> items = new ArrayList<>();

    public void submitList(List<CartItem> newItems) {
        if (newItems == null) {
            items = new ArrayList<>();
        } else {
            items = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_entry, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvQuantity;
        private final TextView tvPrice;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCartCover);
            tvTitle = itemView.findViewById(R.id.tvCartTitle);
            tvDesc = itemView.findViewById(R.id.tvCartDesc);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvPrice = itemView.findViewById(R.id.tvCartPrice);
        }

        void bind(CartItem item) {
            FeedItem product = item.getProduct();
            if (product != null) {
                tvTitle.setText(product.getTitle());
                tvDesc.setText(product.getDescription());
                tvPrice.setVisibility(product.getPriceLabel() != null ? View.VISIBLE : View.GONE);
                if (product.getPriceLabel() != null) {
                    tvPrice.setText(product.getPriceLabel());
                }
                Glide.with(ivCover.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivCover);
            } else {
                tvTitle.setText(R.string.app_name);
                tvDesc.setText("");
                tvPrice.setVisibility(View.GONE);
                ivCover.setImageResource(R.drawable.ic_image_placeholder);
            }
            tvQuantity.setText(itemView.getContext().getString(R.string.cart_quantity_label, item.getQuantity()));
        }
    }
}
