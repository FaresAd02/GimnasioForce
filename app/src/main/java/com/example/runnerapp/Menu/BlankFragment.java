package com.example.runnerapp.Menu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.runnerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
import android.widget.Toast;

public class BlankFragment extends Fragment {

    private RecyclerView recyclerView;
    private TableAdapter adapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    public BlankFragment() {
    }

    public static BlankFragment newInstance() {
        return new BlankFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Datos");

            adapter = new TableAdapter();
            recyclerView.setAdapter(adapter);

            databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Data> dataList = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        String value1 = snapshot.child("datenow").getValue(String.class);
                        float value2 = snapshot.child("metros").getValue(float.class);
                        float value3 = snapshot.child("calorias").getValue(float.class);
                        int value4 = snapshot.child("pasos").getValue(int.class);

                        dataList.add(new Data(value1, value2, value3,value4));
                    }
                    adapter.setDataList(dataList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }

    private static class TableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_DATA = 1;

        private List<Data> dataList = new ArrayList<>();

        public void setDataList(List<Data> dataList) {
            this.dataList = dataList;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_DATA;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    view = inflater.inflate(R.layout.row_table_header, parent, false);
                    return new HeaderViewHolder(view);
                case VIEW_TYPE_DATA:
                    view = inflater.inflate(R.layout.row_table_item, parent, false);
                    return new DataViewHolder(view);
                default:
                    throw new IllegalArgumentException("Invalid view type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DataViewHolder) {
                DataViewHolder dataViewHolder = (DataViewHolder) holder;
                Data data = dataList.get(position);

                dataViewHolder.column1TextView.setText(data.getColumn1());
                dataViewHolder.column2TextView.setText(data.getColumn2());
                dataViewHolder.column3TextView.setText(data.getColumn3());
                dataViewHolder.column4TextView.setText(data.getColumn4());

            }
        }


        @Override
        public int getItemCount() {
            return dataList.size();
        }

        private static class HeaderViewHolder extends RecyclerView.ViewHolder {
            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private static class DataViewHolder extends RecyclerView.ViewHolder {
            TextView column1TextView;
            TextView column2TextView;
            TextView column3TextView;
            TextView column4TextView;

            public DataViewHolder(@NonNull View itemView) {
                super(itemView);
                column1TextView = itemView.findViewById(R.id.text_view_column_1);
                column2TextView = itemView.findViewById(R.id.text_view_column_2);
                column3TextView = itemView.findViewById(R.id.text_view_column_3);
                column4TextView = itemView.findViewById(R.id.text_view_column_4);
            }
        }
    }

    private static class Data {
        private String column1;
        private String column2;
        private String column3;
        private String column4;

        public Data(String column1, float column2, float column3, int column4) {
            this.column1 = column1;
            this.column2 = String.valueOf(column2);
            this.column3 = String.valueOf(column3);
            this.column4 = String.valueOf(column4);
        }

        public String getColumn1() {
            return column1;
        }

        public String getColumn2() {
            return column2;
        }

        public String getColumn3() {
            return column3;
        }
        public String getColumn4() {
            return column4;
        }

    }
}
