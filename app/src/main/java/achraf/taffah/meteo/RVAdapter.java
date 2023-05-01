package achraf.taffah.meteo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Model> arrayList;

    public RVAdapter(Context context, ArrayList<Model> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model model = arrayList.get(position);
        holder.temperatureTV.setText(model.getTemperature()+"°C");
        Picasso.get().load("http:".concat(model.getIcon())).into(holder.conditionTV);
        holder.windTv.setText(model.getWindSpeed()+"Km/h");
        holder.humidityTV.setText(model.getHumidity()+"%");
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
        try{
            Date date = input.parse(model.getTime());
            holder.timeTV.setText(output.format(date));
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView windTv, temperatureTV,timeTV,humidityTV;
        private ImageView conditionTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            windTv = itemView.findViewById(R.id.idTVwindSpeed);
            temperatureTV = itemView.findViewById(R.id.idTVtemperature);
            timeTV = itemView.findViewById(R.id.idTVtime);
            conditionTV = itemView.findViewById(R.id.idIVcondition);
            humidityTV = itemView.findViewById(R.id.idTVhumidity);
        }
    }
}
