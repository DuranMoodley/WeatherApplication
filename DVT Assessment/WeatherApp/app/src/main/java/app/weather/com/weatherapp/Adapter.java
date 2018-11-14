package app.weather.com.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Adapter  extends ArrayAdapter<String>
{
    ArrayList days;
    ArrayList temp;
    Context context;
    String weatherDecription;
    public Adapter(Context cont, ArrayList days, ArrayList temp,String weatherDecription){
        super(cont,R.layout.listitem,days);
        this.days = days;
        this.temp = temp;
        context = cont;
        this.weatherDecription = weatherDecription;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View content = view;

        if(content == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            content = inflater.inflate(R.layout.listitem,null);
        }
        LinearLayout layout = (LinearLayout) content.findViewById(R.id.main);
        RelativeLayout innerLayout = (RelativeLayout)content.findViewById(R.id.inner);
        TextView tvDay = (TextView) content.findViewById(R.id.day);
        TextView tvTemp = (TextView) content.findViewById(R.id.tvTemp);
        ImageView imageView = (ImageView) content.findViewById(R.id.imgWeatherType);
        if(weatherDecription.equals("Clear")){
            imageView.setImageResource(R.drawable.clear);
            layout.setBackgroundResource(R.color.sunny);
            innerLayout.setBackgroundResource(R.color.sunny);
        }
        else if(weatherDecription.equals("Rain")){
            imageView.setImageResource(R.drawable.rain);
            layout.setBackgroundResource(R.color.rainy);
            innerLayout.setBackgroundResource(R.color.rainy);
        }
        else {
            imageView.setImageResource(R.drawable.partlysunny);
            layout.setBackgroundResource(R.color.cloudy);
            innerLayout.setBackgroundResource(R.color.cloudy);
        }


        tvDay.setText(days.get(i).toString());
        tvTemp.setText(String.format("%.0f",Float.parseFloat(temp.get(i).toString()))+ (char) 0x00B0);
        return content;
    }
}
