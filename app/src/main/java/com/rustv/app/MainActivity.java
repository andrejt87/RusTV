package com.rustv.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Channel> channels = loadChannels();
        List<Object> items = buildCategorizedList(channels);

        ChannelAdapter adapter = new ChannelAdapter(this, items, channels);
        recyclerView.setAdapter(adapter);
    }

    private List<Channel> loadChannels() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open("channels.json")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            Type listType = new TypeToken<List<Channel>>(){}.getType();
            return new Gson().fromJson(sb.toString(), listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Object> buildCategorizedList(List<Channel> channels) {
        LinkedHashMap<String, List<Channel>> categoryMap = new LinkedHashMap<>();
        for (Channel ch : channels) {
            if (!categoryMap.containsKey(ch.category)) {
                categoryMap.put(ch.category, new ArrayList<>());
            }
            categoryMap.get(ch.category).add(ch);
        }

        List<Object> items = new ArrayList<>();
        for (Map.Entry<String, List<Channel>> entry : categoryMap.entrySet()) {
            items.add(entry.getKey()); // category header
            items.addAll(entry.getValue());
        }
        return items;
    }

    // --- Adapter ---

    static class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_CHANNEL = 1;

        private final Context context;
        private final List<Object> items;
        private final List<Channel> allChannels;

        ChannelAdapter(Context context, List<Object> items, List<Channel> allChannels) {
            this.context = context;
            this.items = items;
            this.allChannels = allChannels;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_HEADER : TYPE_CHANNEL;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View view = inflater.inflate(R.layout.item_category_header, parent, false);
                return new HeaderViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_channel, parent, false);
                return new ChannelViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).bind((String) items.get(position));
            } else {
                Channel channel = (Channel) items.get(position);
                ((ChannelViewHolder) holder).bind(channel);
                holder.itemView.setOnClickListener(v -> {
                    int channelIndex = allChannels.indexOf(channel);
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("url", channel.url);
                    intent.putExtra("title", channel.title);
                    intent.putExtra("channelIndex", channelIndex);
                    context.startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            HeaderViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.category_title);
            }
            void bind(String category) {
                textView.setText(category);
            }
        }

        static class ChannelViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            TextView arrowView;
            View accentView;
            ChannelViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.channel_title);
                arrowView = itemView.findViewById(R.id.channel_arrow);
                accentView = itemView.findViewById(R.id.channel_accent);

                // Focus change listener for D-pad navigation
                itemView.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        textView.setTextColor(0xFFFFFFFF);
                        if (arrowView != null) arrowView.setTextColor(0xFFFFFFFF);
                        if (accentView != null) accentView.setBackgroundColor(0xFFFFFF00);
                        v.animate().scaleX(1.03f).scaleY(1.03f).setDuration(150).start();
                    } else {
                        textView.setTextColor(0xFFE0E0E0);
                        if (arrowView != null) arrowView.setTextColor(0xFF666666);
                        if (accentView != null) accentView.setBackgroundColor(0xFFFF4444);
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    }
                });
            }
            void bind(Channel channel) {
                textView.setText(channel.title);
            }
        }
    }
}
