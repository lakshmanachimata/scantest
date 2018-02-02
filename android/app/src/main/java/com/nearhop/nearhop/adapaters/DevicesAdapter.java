package com.nearhop.nearhop.adapaters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nearhop.nearhop.R;
import com.nearhop.nearhop.utilities.Host;

import java.util.ArrayList;


/**
 * Receipts Adapter For Showing the Receipts
 * it also tells about the receipts is processed or not
 * <p>
 * Created by lakshmana on 12/11/17.
 * <p>
 * Edited by shubham on 06/12/2017
 */

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ReceiptsViewHolder> {
    private static final String TAG = DevicesAdapter.class.getSimpleName();
    private Context mContext; //context
    private ArrayList<Host> hostSList = new ArrayList<>(); //data source of the list adapter
    private View mReceiptView;

    //public constructor
    public DevicesAdapter(Context context, ArrayList<Host> hostList) {
        this.mContext = context;
        hostSList.clear();
        hostSList = hostList;
    }

    @Override
    public ReceiptsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mReceiptView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new ReceiptsViewHolder(mReceiptView);
    }

    @Override
    public void onBindViewHolder(ReceiptsViewHolder holder, int position) {

        Host data = hostSList.get(position);
        holder.hostName.setText(data.getIp());
        holder.portNum.setText(data.getPort());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return hostSList.size();//returns total of items in the list
    }

    class ReceiptsViewHolder extends RecyclerView.ViewHolder {
        ImageView deviceImage;
        TextView hostName, portNum;

        public ReceiptsViewHolder(View itemView) {
            super(itemView);
            deviceImage = (ImageView) itemView.findViewById(R.id.device_image);
            hostName = (TextView) itemView.findViewById(R.id.hostname);
            portNum = (TextView) itemView.findViewById(R.id.portcheck);

        }
    }
}
