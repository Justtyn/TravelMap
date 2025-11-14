package com.justyn.travelmap.ui.feed;

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
import com.justyn.travelmap.model.FeedItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    public interface OnItemClickListener {
        void onFeedItemClicked(@NonNull FeedItem item);
    }

    private final OnItemClickListener listener;
    private List<FeedItem> items = new ArrayList<>();

    public FeedAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FeedItem> newItems) {
        if (newItems == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_card, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class FeedViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivCover;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final TextView tvMeta;
        private final TextView tvStock;
        private final TextView tvPrice;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvStock = itemView.findViewById(R.id.tvStock);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(FeedItem item) {
            tvTitle.setText(item.getTitle());
            String description = item.getDescription();
            tvDesc.setText(description != null ? description : "");
            String metaText = buildMetaText(item);
            if (metaText == null || metaText.isEmpty()) {
                tvMeta.setVisibility(View.GONE);
            } else {
                tvMeta.setVisibility(View.VISIBLE);
                tvMeta.setText(metaText);
            }
            Integer stock = item.getStock();
            if (stock != null && stock >= 0) {
                tvStock.setVisibility(View.VISIBLE);
                tvStock.setText(itemView.getContext().getString(R.string.feed_stock_label, stock));
            } else {
                tvStock.setVisibility(View.GONE);
            }
            String priceLabel = item.getPriceLabel();
            if (priceLabel == null || priceLabel.isEmpty()) {
                tvPrice.setVisibility(View.GONE);
            } else {
                tvPrice.setVisibility(View.VISIBLE);
                tvPrice.setText(priceLabel);
            }
            Glide.with(ivCover.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivCover);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFeedItemClicked(item);
                }
            });
        }

        private String buildMetaText(FeedItem item) {
            StringBuilder builder = new StringBuilder();
            if (item.getAddress() != null && !item.getAddress().isEmpty()) {
                builder.append(item.getAddress());
            } else if (item.getExtraInfo() != null && !item.getExtraInfo().isEmpty()) {
                builder.append(item.getExtraInfo());
            }
            Double lat = item.getLatitude();
            Double lng = item.getLongitude();
            if (lat != null && lng != null) {
                if (builder.length() > 0) {
                    builder.append(" · ");
                }
                builder.append(String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng));
            }
            return builder.toString();
        }
    }
}
