package com.firstrowria.android.vpn.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firstrowria.android.vpn.R;
import com.firstrowria.android.vpn.vo.VPNServer;

import java.util.ArrayList;

/**
 * Created by Bernd on 1/6/2016.
 */
public class VPNServerAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<VPNServer> servers;
    private Context context;

    public VPNServerAdapter(Context context, ArrayList<VPNServer> servers) {
        this.context = context;
        this.servers = servers;
        this.inflater = LayoutInflater.from(context);
    }

//	@Override
//	public boolean areAllItemsEnabled()
//	{
//		return false;
//	}

    @Override
    public int getCount() {
        return servers.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_server, null);

            holder = new ViewHolder();
            holder.locationTextView = (TextView) convertView.findViewById(R.id.locationTextView);
            holder.hostTextView = (TextView) convertView.findViewById(R.id.hostTextView);
            holder.loadTextView = (TextView) convertView.findViewById(R.id.loadTextView);
            holder.loadProgressBar = (ProgressBar) convertView.findViewById(R.id.loadProgressBar);
            //holder.clickListener = new OpenURLClickListener(context, null, analyticsCategory, GoogleAnalytics.EVENT_ACTION_VIDEOS, null);

            //convertView.setOnClickListener(holder.clickListener);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        VPNServer vpnServer = servers.get(position);

        //TextView currentTextView = (TextView) tableRow.findViewById(R.id.titleTextView);
        holder.hostTextView.setText(vpnServer.host);
        holder.locationTextView.setText(vpnServer.country);
        holder.loadTextView.setText(vpnServer.load);
        holder.loadProgressBar.setProgress(vpnServer.loadPercentage);

        if (vpnServer.loadPercentage <= 5)
            holder.loadProgressBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.loadLow)));
        else if (vpnServer.loadPercentage <= 10)
            holder.loadProgressBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.loadMedium)));
        else
            holder.loadProgressBar.setProgressTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.loadHigh)));

        //Log.d("MainActivity", "load: " + vpnServer.loadPercentage);

        return convertView;
    }

    public static class ViewHolder {
        private TextView locationTextView;
        private TextView hostTextView;
        private TextView loadTextView;
        private ProgressBar loadProgressBar;
    }

}
