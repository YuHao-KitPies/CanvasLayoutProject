package org.kitpies.canvaslayout.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.kitpies.canvaslayout.R;

/**
 * Adapter for test.
 */
public class TestAdapter extends BaseAdapter{

    private Context mContext;

    public TestAdapter(Context context){
        mContext = context;
    }

    @Override
    public int getCount() {
        return 10;
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
        View canvasLayout;
        if(convertView!=null){
            canvasLayout = convertView;
        }else {
            canvasLayout = View.inflate(mContext, R.layout.item_view,null);
        }

        return canvasLayout;
    }
}
