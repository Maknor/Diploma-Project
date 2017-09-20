package savushkin.by.edi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class StateAdapter extends ArrayAdapter<State> {

    private LayoutInflater inflater;
    private int layout;
    private List<State> states;
    public StateAdapter(Context context, int resource, List<State> states) {
        super(context, resource, states);
        this.states = states;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater.inflate(this.layout, parent, false);

        TextView NameView = (TextView) view.findViewById(R.id.NameDoc);
        TextView NumView = (TextView) view.findViewById(R.id.NumDoc);
        TextView DataView = (TextView) view.findViewById(R.id.DataDoc);

        State state = states.get(position);

        NameView.setText(state.getNameDoc());
        NumView.setText(state.getNumDoc());
        DataView.setText(state.getDataDoc());


        if (EDI.fcolor) {
            NameView.setBackgroundColor(Color.RED);
            NameView.setTextColor(Color.CYAN);
            NumView.setBackgroundColor(Color.RED);
            NumView.setTextColor(Color.BLACK);
            DataView.setBackgroundColor(Color.RED);
            DataView.setTextColor(Color.CYAN);

        } else {
            NameView.setBackgroundColor(Color.YELLOW);
            NameView.setTextColor(Color.BLUE);
            NumView.setBackgroundColor(Color.YELLOW);
            NumView.setTextColor(Color.MAGENTA);
            DataView.setBackgroundColor(Color.YELLOW);
            DataView.setTextColor(Color.BLUE);
        }
        NameView.setTextSize(SharedPref());
        NumView.setTextSize(SharedPref());
        DataView.setTextSize(SharedPref());

        String style = EDI.style.toString();
        int typeface = Typeface.NORMAL;

        if (style.contains("Полужирный"))
            typeface += Typeface.BOLD;

        if (style.contains("Курсив"))
            typeface += Typeface.ITALIC;

        NameView.setTypeface(null, typeface);
        NumView.setTypeface(null, typeface);
        DataView.setTypeface(null, typeface);

        return view;
    }

    private float SharedPref() {
        float fSize = Float.parseFloat(EDI.fSize.toString());
        return fSize;

    }

}