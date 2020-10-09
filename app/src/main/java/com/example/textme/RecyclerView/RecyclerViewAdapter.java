/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.textme.RecyclerView;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.textme.R;


// Provide views to RecyclerView with data from mDataSet.

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private String[] mDataSet;
    private int[] mDataIndex;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView numberView;

        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.textView);
            numberView = v.findViewById(R.id.numberView);
        }

        public TextView getTextView() {
            return textView;
        }

        public TextView getNumberView() {
            return numberView;
        }
    }

    public RecyclerViewAdapter(String[] dataSet, int[] dataIndex) {
        mDataIndex = dataIndex;
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(mDataSet[position]);
        viewHolder.getNumberView().setText(Integer.toString(mDataIndex[position]));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }
}
